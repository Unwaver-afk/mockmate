"""Pydantic request/response models matching the Android Retrofit contracts."""

from pydantic import BaseModel
from typing import Optional


# ── Interview flow ────────────────────────────────────────────────

class InterviewStartRequest(BaseModel):
    company: str
    role: str
    difficulty: str
    type: str


class InterviewMessage(BaseModel):
    role: str        # "ai" or "user"
    content: str


class InterviewMessageRequest(BaseModel):
    company: str
    role: str
    difficulty: str
    type: str
    messages: list[InterviewMessage]
    answer: str


class InterviewEndRequest(BaseModel):
    company: str
    role: str
    difficulty: str
    type: str
    messages: list[InterviewMessage]


class InterviewResponse(BaseModel):
    message: str


# ── Auth ──────────────────────────────────────────────────────────

class LoginRequest(BaseModel):
    name: str
    email: str
    college: Optional[str] = "IIT Delhi"


class LoginResponse(BaseModel):
    user_id: int
    name: str
    email: str
    college: str


# ── Dashboard ─────────────────────────────────────────────────────

class DashboardResponse(BaseModel):
    name: str
    streak: int
    average_score: int
    total_interviews: int
    areas: list[dict]
    history: list[dict]


# ── Leaderboard ───────────────────────────────────────────────────

class LeaderboardEntry(BaseModel):
    rank: int
    name: str
    department: str
    points: int
    is_current_user: bool = False


class LeaderboardResponse(BaseModel):
    your_rank: int
    total_students: int
    your_points: int
    entries: list[LeaderboardEntry]
