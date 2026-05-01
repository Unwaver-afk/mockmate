"""
MockMate Backend — FastAPI + Gemini + SQLite
Run: uvicorn main:app --reload --host 0.0.0.0 --port 8000
"""

import json
import os
import traceback
from contextlib import asynccontextmanager
from datetime import datetime

import google.generativeai as genai
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from database import (
    get_leaderboard,
    get_user,
    get_user_history,
    get_user_rank,
    get_user_stats,
    init_db,
    save_interview,
    upsert_user,
)
from models import (
    DashboardResponse,
    InterviewEndRequest,
    InterviewMessageRequest,
    InterviewResponse,
    InterviewStartRequest,
    LeaderboardResponse,
    LoginRequest,
    LoginResponse,
)
from prompts import REPORT_SYSTEM_PROMPT, interviewer_system_prompt

# ── Configuration ─────────────────────────────────────────────────

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)

MODEL_CHAIN = ["gemini-2.0-flash", "gemini-2.5-flash", "gemini-2.0-flash-lite"]


# ── App lifecycle ─────────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Initialize DB on startup."""
    await init_db()
    await _seed_demo_data()
    print("✅ MockMate backend ready — http://localhost:8000/docs")
    yield

app = FastAPI(
    title="MockMate API",
    description="AI-powered mock interview backend",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Gemini helper ─────────────────────────────────────────────────

import asyncio

def _build_gemini_history(system_prompt: str, messages: list[dict]) -> list[dict]:
    """Convert our message list into Gemini chat history format."""
    history = [
        {"role": "user", "parts": [system_prompt]},
        {"role": "model", "parts": ["Understood. I will conduct the interview in character. Let me begin."]},
    ]
    for msg in messages:
        role = "user" if msg.get("role") == "user" else "model"
        history.append({"role": role, "parts": [msg["content"]]})
    return history


async def _gemini_chat(system_prompt: str, messages: list[dict], user_input: str) -> str:
    """Send a message to Gemini. Tries each model in MODEL_CHAIN until one works."""
    if not GEMINI_API_KEY:
        raise ValueError("No GEMINI_API_KEY configured")

    last_error = None
    for model_name in MODEL_CHAIN:
        try:
            model = genai.GenerativeModel(model_name)
            history = _build_gemini_history(system_prompt, messages[:-1] if messages else [])
            chat = model.start_chat(history=history)
            response = chat.send_message(user_input)
            return response.text.strip()
        except Exception as e:
            last_error = e
            err_str = str(e)
            if "429" in err_str or "quota" in err_str.lower():
                print(f"⚠️ {model_name} quota exceeded, trying next model...")
                continue
            raise  # Non-quota errors should propagate

    raise last_error  # All models exhausted


async def _gemini_generate(prompt: str) -> str:
    """One-shot generation with model fallback chain."""
    if not GEMINI_API_KEY:
        raise ValueError("No GEMINI_API_KEY configured")

    last_error = None
    for model_name in MODEL_CHAIN:
        try:
            model = genai.GenerativeModel(model_name)
            response = model.generate_content(prompt)
            return response.text.strip()
        except Exception as e:
            last_error = e
            err_str = str(e)
            if "429" in err_str or "quota" in err_str.lower():
                print(f"⚠️ {model_name} quota exceeded, trying next model...")
                continue
            raise

    raise last_error


# ═══════════════════════════════════════════════════════════════════
#  INTERVIEW ENDPOINTS (match Android Retrofit contracts)
# ═══════════════════════════════════════════════════════════════════

@app.post("/api/interview/start", response_model=InterviewResponse)
async def start_interview(req: InterviewStartRequest):
    """Start a new interview session — returns the first AI question."""
    try:
        system = interviewer_system_prompt(req.company, req.role, req.difficulty, req.type)
        prompt = (
            f"Start the {req.type} interview for {req.role} at {req.company} "
            f"({req.difficulty} level). Greet the candidate briefly and ask your first question."
        )

        last_error = None
        for model_name in MODEL_CHAIN:
            try:
                model = genai.GenerativeModel(model_name)
                chat = model.start_chat(history=[
                    {"role": "user", "parts": [system]},
                    {"role": "model", "parts": ["Understood. I'll conduct the interview now."]},
                ])
                response = chat.send_message(prompt)
                return InterviewResponse(message=response.text.strip())
            except Exception as e:
                last_error = e
                if "429" in str(e) or "quota" in str(e).lower():
                    print(f"⚠️ {model_name} quota exceeded, trying next...")
                    continue
                raise
        raise last_error

    except Exception as e:
        print(f"⚠️ Gemini error in /start: {e}")
        return InterviewResponse(
            message=f"Welcome to your {req.type} interview for {req.role} at {req.company}. "
                    f"Let's get started. Tell me about yourself and why you're interested in this role."
        )


@app.post("/api/interview/message", response_model=InterviewResponse)
async def send_message(req: InterviewMessageRequest):
    """Send a candidate answer and get the next AI interviewer question."""
    try:
        system = interviewer_system_prompt(req.company, req.role, req.difficulty, req.type)
        messages = [{"role": m.role, "content": m.content} for m in req.messages]
        response_text = await _gemini_chat(system, messages, req.answer)
        return InterviewResponse(message=response_text)

    except Exception as e:
        print(f"⚠️ Gemini error in /message: {e}")
        return InterviewResponse(
            message="That's an interesting point. Can you elaborate on the specific "
                    "technical challenges you faced and how you overcame them?"
        )


@app.post("/api/interview/end")
async def end_interview(req: InterviewEndRequest):
    """End the interview and generate a full performance report."""
    try:
        # Build the transcript
        transcript_lines = []
        for msg in req.messages:
            speaker = "Interviewer" if msg.role == "ai" else "Candidate"
            transcript_lines.append(f"{speaker}: {msg.content}")
        transcript = "\n\n".join(transcript_lines)

        # Ask Gemini to generate the report
        today = datetime.now().strftime("%B %d, %Y")
        prompt = (
            f"{REPORT_SYSTEM_PROMPT}\n\n"
            f"═══ INTERVIEW CONTEXT ═══\n"
            f"Company: {req.company}\n"
            f"Role: {req.role}\n"
            f"Difficulty: {req.difficulty}\n"
            f"Type: {req.type}\n"
            f"Date: {today}\n\n"
            f"═══ FULL TRANSCRIPT ═══\n\n{transcript}"
        )

        raw_response = await _gemini_generate(prompt)

        # Clean up: Gemini sometimes wraps JSON in markdown code fences
        cleaned = raw_response
        if "```json" in cleaned:
            cleaned = cleaned.split("```json")[1].split("```")[0]
        elif "```" in cleaned:
            cleaned = cleaned.split("```")[1].split("```")[0]
        cleaned = cleaned.strip()

        report = json.loads(cleaned)

        # Ensure required fields exist with sensible defaults
        report.setdefault("roleCompany", f"{req.role} at {req.company}")
        report.setdefault("date", today)
        report.setdefault("score", 75)
        report.setdefault("communication", 75)
        report.setdefault("relevance", 75)
        report.setdefault("technical", 75)
        report.setdefault("summary", "Good effort overall.")
        report.setdefault("focus", [])
        report.setdefault("answers", [])

        return report

    except json.JSONDecodeError as e:
        print(f"⚠️ JSON parse error: {e}\nRaw response: {raw_response[:500]}")
        return _fallback_report(req)
    except Exception as e:
        print(f"⚠️ Gemini error in /end: {e}")
        traceback.print_exc()
        return _fallback_report(req)


def _fallback_report(req: InterviewEndRequest) -> dict:
    """Generate a reasonable fallback report if Gemini fails."""
    user_answers = [m for m in req.messages if m.role == "user"]
    score = min(94, max(65, 72 + len(user_answers) * 3))
    today = datetime.now().strftime("%B %d, %Y")

    answers = []
    for i, msg in enumerate(user_answers):
        q_index = i * 2
        question = req.messages[q_index].content if q_index < len(req.messages) else f"Question {i+1}"
        answers.append({
            "question": question,
            "answer": msg.content,
            "idealAnswer": "A strong answer would set context, clarify constraints, "
                          "give a structured example with metrics, and close with a recommendation.",
            "score": min(90, score - 3 + i * 2)
        })

    return {
        "roleCompany": f"{req.role} at {req.company}",
        "date": today,
        "score": score,
        "communication": min(96, score + 4),
        "relevance": max(60, score - 3),
        "technical": min(94, score + 1),
        "summary": "You demonstrated solid fundamentals. Focus on adding specific metrics, "
                   "clarifying constraints earlier, and structuring answers more decisively.",
        "focus": [
            {"title": "Add Measurable Evidence", "description": "Quantify impact in every answer — numbers, percentages, time saved."},
            {"title": "Clarify Constraints First", "description": "Before solving, state your assumptions, scope, and success criteria."},
            {"title": "Tighten Conclusions", "description": "Close each answer with a crisp decision and the tradeoff behind it."},
        ],
        "answers": answers,
    }


# ═══════════════════════════════════════════════════════════════════
#  AUTH ENDPOINT
# ═══════════════════════════════════════════════════════════════════

@app.post("/api/auth/login", response_model=LoginResponse)
async def login(req: LoginRequest):
    """Demo login — create or return user. No real auth required."""
    user = await upsert_user(
        name=req.name.strip() or "Alex",
        email=req.email.strip() or "alex@demo.com",
        college=req.college or "IIT Delhi",
    )
    return LoginResponse(
        user_id=user["id"],
        name=user["name"],
        email=user["email"],
        college=user["college"],
    )


# ═══════════════════════════════════════════════════════════════════
#  DASHBOARD ENDPOINT
# ═══════════════════════════════════════════════════════════════════

@app.get("/api/user/{user_id}/dashboard", response_model=DashboardResponse)
async def get_dashboard(user_id: int):
    """Get dashboard data: streak, avg score, total interviews, areas, history."""
    user = await get_user(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    stats = await get_user_stats(user_id)
    history_raw = await get_user_history(user_id, limit=5)

    history = []
    for h in history_raw:
        history.append({
            "role": h["role"],
            "companyDate": f"{h['company'].upper()} {h['type'].upper()} • {h['created_at'][:10]}",
            "score": h["score"] or 0,
        })

    return DashboardResponse(
        name=user["name"],
        streak=stats["streak"],
        average_score=stats["average_score"],
        total_interviews=stats["total_interviews"],
        areas=stats["areas"],
        history=history,
    )


# ═══════════════════════════════════════════════════════════════════
#  INTERVIEW SAVE ENDPOINT
# ═══════════════════════════════════════════════════════════════════

@app.post("/api/interview/save")
async def save_interview_result(
    user_id: int,
    company: str,
    role: str,
    difficulty: str,
    interview_type: str,
    report: dict,
):
    """Save a completed interview report to the database."""
    interview_id = await save_interview(
        user_id=user_id,
        company=company,
        role=role,
        difficulty=difficulty,
        interview_type=interview_type,
        score=report.get("score", 0),
        communication=report.get("communication", 0),
        relevance=report.get("relevance", 0),
        technical=report.get("technical", 0),
        summary=report.get("summary", ""),
        report_json=json.dumps(report),
    )
    return {"interview_id": interview_id, "status": "saved"}


# ═══════════════════════════════════════════════════════════════════
#  LEADERBOARD ENDPOINT
# ═══════════════════════════════════════════════════════════════════

@app.get("/api/leaderboard", response_model=LeaderboardResponse)
async def leaderboard(college: str = "IIT Delhi", range: str = "weekly", user_id: int = 1):
    """Get the college leaderboard with the current user's rank."""
    entries = await get_leaderboard(college, range)
    user_rank = await get_user_rank(user_id, college, range)

    leaderboard_entries = []
    for entry in entries:
        leaderboard_entries.append({
            "rank": entry["rank"],
            "name": entry["name"],
            "department": entry["department"],
            "points": entry["points"],
            "is_current_user": False,
        })

    return LeaderboardResponse(
        your_rank=user_rank["rank"],
        total_students=user_rank["total"],
        your_points=user_rank["points"],
        entries=leaderboard_entries,
    )


# ═══════════════════════════════════════════════════════════════════
#  HISTORY ENDPOINT
# ═══════════════════════════════════════════════════════════════════

@app.get("/api/user/{user_id}/history")
async def history(user_id: int, limit: int = 20):
    """Get interview history for a user."""
    rows = await get_user_history(user_id, limit)
    return {"interviews": rows}


# ═══════════════════════════════════════════════════════════════════
#  SEED DEMO DATA
# ═══════════════════════════════════════════════════════════════════

async def _seed_demo_data():
    """Seed the database with demo users and interviews for the leaderboard."""
    from database import get_db

    db = await get_db()
    try:
        cursor = await db.execute("SELECT COUNT(*) as c FROM users")
        row = await cursor.fetchone()
        if row["c"] > 0:
            return  # Already seeded

        # Create demo users
        demo_users = [
            ("Alex Johnson", "alex@demo.com", "IIT Delhi"),
            ("Priya Sharma", "priya@demo.com", "IIT Delhi"),
            ("Rahul Gupta", "rahul@demo.com", "IIT Delhi"),
            ("Sneha Patel", "sneha@demo.com", "IIT Delhi"),
            ("Arjun Singh", "arjun@demo.com", "IIT Delhi"),
            ("Kavya Nair", "kavya@demo.com", "IIT Delhi"),
            ("Vikram Reddy", "vikram@demo.com", "IIT Delhi"),
            ("Ananya Das", "ananya@demo.com", "IIT Delhi"),
            ("Rohan Mehta", "rohan@demo.com", "IIT Delhi"),
            ("Ishita Joshi", "ishita@demo.com", "IIT Delhi"),
        ]
        for name, email, college in demo_users:
            await db.execute(
                "INSERT INTO users (name, email, college) VALUES (?, ?, ?)",
                (name, email, college)
            )

        # Seed leaderboard scores
        scores = [
            (1, "IIT Delhi", 892, 3240, 24),  # Alex — current user
            (2, "IIT Delhi", 945, 3580, 28),
            (3, "IIT Delhi", 876, 3120, 22),
            (4, "IIT Delhi", 834, 2980, 20),
            (5, "IIT Delhi", 812, 2850, 19),
            (6, "IIT Delhi", 798, 2710, 18),
            (7, "IIT Delhi", 756, 2560, 16),
            (8, "IIT Delhi", 723, 2400, 15),
            (9, "IIT Delhi", 690, 2250, 14),
            (10, "IIT Delhi", 654, 2100, 12),
        ]
        for uid, college, weekly, monthly, total in scores:
            await db.execute(
                """INSERT INTO leaderboard (user_id, college, weekly_score, monthly_score, total_interviews)
                   VALUES (?, ?, ?, ?, ?)""",
                (uid, college, weekly, monthly, total)
            )

        # Seed a few interviews for Alex (user_id=1) so dashboard has data
        demo_interviews = [
            (1, "Google", "Software Engineer", "Fresher", "Technical", 88, 92, 85, 87,
             "Strong technical fundamentals with good communication."),
            (1, "TCS", "Backend Dev", "Fresher", "Technical", 76, 80, 72, 74,
             "Good effort. Needs more depth in system design."),
            (1, "Amazon", "Product Manager", "1-year exp", "HR", 92, 95, 90, 88,
             "Excellent behavioral responses with clear STAR structure."),
        ]
        for args in demo_interviews:
            await db.execute(
                """INSERT INTO interviews
                   (user_id, company, role, difficulty, type, score,
                    communication, relevance, technical, summary)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                args
            )

        await db.commit()
        print("🌱 Seeded demo data: 10 users, leaderboard, 3 interviews for Alex")
    finally:
        await db.close()
