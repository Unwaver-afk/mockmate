"""SQLite database layer for persistent storage of users, interviews, and leaderboard."""

import aiosqlite
import os
from datetime import datetime, timedelta

DB_PATH = os.path.join(os.path.dirname(__file__), "mockmate.db")


async def get_db() -> aiosqlite.Connection:
    """Get a connection to the SQLite database."""
    db = await aiosqlite.connect(DB_PATH)
    db.row_factory = aiosqlite.Row
    await db.execute("PRAGMA journal_mode=WAL")
    await db.execute("PRAGMA foreign_keys=ON")
    return db


async def init_db():
    """Create all tables if they don't exist."""
    db = await get_db()
    try:
        await db.executescript("""
            CREATE TABLE IF NOT EXISTS users (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                name        TEXT NOT NULL,
                email       TEXT UNIQUE NOT NULL,
                college     TEXT NOT NULL DEFAULT 'IIT Delhi',
                created_at  TEXT NOT NULL DEFAULT (datetime('now'))
            );

            CREATE TABLE IF NOT EXISTS interviews (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id         INTEGER NOT NULL,
                company         TEXT NOT NULL,
                role            TEXT NOT NULL,
                difficulty      TEXT NOT NULL,
                type            TEXT NOT NULL,
                score           INTEGER,
                communication   INTEGER,
                relevance       INTEGER,
                technical       INTEGER,
                summary         TEXT,
                report_json     TEXT,
                created_at      TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );

            CREATE TABLE IF NOT EXISTS leaderboard (
                user_id         INTEGER PRIMARY KEY,
                college         TEXT NOT NULL,
                weekly_score    INTEGER NOT NULL DEFAULT 0,
                monthly_score   INTEGER NOT NULL DEFAULT 0,
                total_interviews INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );

            CREATE INDEX IF NOT EXISTS idx_interviews_user ON interviews(user_id);
            CREATE INDEX IF NOT EXISTS idx_leaderboard_college ON leaderboard(college);
        """)
        await db.commit()
    finally:
        await db.close()


# ── User operations ───────────────────────────────────────────────

async def upsert_user(name: str, email: str, college: str = "IIT Delhi") -> dict:
    """Create a user or return existing one. Also ensure leaderboard row exists."""
    db = await get_db()
    try:
        cursor = await db.execute("SELECT * FROM users WHERE email = ?", (email,))
        row = await cursor.fetchone()
        if row:
            return dict(row)

        cursor = await db.execute(
            "INSERT INTO users (name, email, college) VALUES (?, ?, ?)",
            (name, email, college)
        )
        user_id = cursor.lastrowid
        await db.execute(
            "INSERT OR IGNORE INTO leaderboard (user_id, college) VALUES (?, ?)",
            (user_id, college)
        )
        await db.commit()

        cursor = await db.execute("SELECT * FROM users WHERE id = ?", (user_id,))
        return dict(await cursor.fetchone())
    finally:
        await db.close()


async def get_user(user_id: int) -> dict | None:
    """Fetch a user by ID."""
    db = await get_db()
    try:
        cursor = await db.execute("SELECT * FROM users WHERE id = ?", (user_id,))
        row = await cursor.fetchone()
        return dict(row) if row else None
    finally:
        await db.close()


# ── Interview operations ──────────────────────────────────────────

async def save_interview(
    user_id: int,
    company: str,
    role: str,
    difficulty: str,
    interview_type: str,
    score: int,
    communication: int,
    relevance: int,
    technical: int,
    summary: str,
    report_json: str,
) -> int:
    """Save a completed interview and update leaderboard scores."""
    db = await get_db()
    try:
        cursor = await db.execute(
            """INSERT INTO interviews
               (user_id, company, role, difficulty, type, score,
                communication, relevance, technical, summary, report_json)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (user_id, company, role, difficulty, interview_type,
             score, communication, relevance, technical, summary, report_json)
        )
        interview_id = cursor.lastrowid

        # Update leaderboard
        await db.execute("""
            UPDATE leaderboard SET
                weekly_score = weekly_score + ?,
                monthly_score = monthly_score + ?,
                total_interviews = total_interviews + 1
            WHERE user_id = ?
        """, (score, score, user_id))

        await db.commit()
        return interview_id
    finally:
        await db.close()


async def get_user_history(user_id: int, limit: int = 10) -> list[dict]:
    """Get recent interview history for a user."""
    db = await get_db()
    try:
        cursor = await db.execute(
            """SELECT id, company, role, type, score, created_at
               FROM interviews WHERE user_id = ?
               ORDER BY created_at DESC LIMIT ?""",
            (user_id, limit)
        )
        rows = await cursor.fetchall()
        return [dict(r) for r in rows]
    finally:
        await db.close()


async def get_user_stats(user_id: int) -> dict:
    """Get aggregate stats for the dashboard."""
    db = await get_db()
    try:
        # Average score
        cursor = await db.execute(
            "SELECT AVG(score) as avg, COUNT(*) as total FROM interviews WHERE user_id = ?",
            (user_id,)
        )
        row = await cursor.fetchone()
        avg_score = int(row["avg"]) if row["avg"] else 0
        total = row["total"] or 0

        # Streak calculation: count consecutive days with at least 1 interview
        cursor = await db.execute(
            """SELECT DISTINCT DATE(created_at) as d
               FROM interviews WHERE user_id = ?
               ORDER BY d DESC LIMIT 30""",
            (user_id,)
        )
        dates = [r["d"] for r in await cursor.fetchall()]
        streak = 0
        today = datetime.utcnow().date()
        for i, d in enumerate(dates):
            expected = (today - timedelta(days=i)).isoformat()
            if d == expected:
                streak += 1
            else:
                break

        # Weak areas (average per sub-score)
        cursor = await db.execute(
            """SELECT AVG(communication) as comm, AVG(relevance) as rel,
                      AVG(technical) as tech
               FROM interviews WHERE user_id = ?""",
            (user_id,)
        )
        scores = await cursor.fetchone()

        areas = []
        if scores["comm"]:
            areas.append({"label": "Communication", "status": _status(scores["comm"]), "progress": round(scores["comm"] / 100, 2)})
            areas.append({"label": "Relevance", "status": _status(scores["rel"]), "progress": round(scores["rel"] / 100, 2)})
            areas.append({"label": "Technical Accuracy", "status": _status(scores["tech"]), "progress": round(scores["tech"] / 100, 2)})

        return {
            "average_score": avg_score,
            "total_interviews": total,
            "streak": streak if streak > 0 else 1,  # show at least 1 if they've done any
            "areas": areas,
        }
    finally:
        await db.close()


def _status(score) -> str:
    """Convert a score to a human-readable status."""
    s = float(score) if score else 0
    if s >= 85:
        return "Excellent"
    elif s >= 70:
        return "Almost There"
    elif s >= 50:
        return "Needs Work"
    else:
        return "Critical"


# ── Leaderboard operations ────────────────────────────────────────

async def get_leaderboard(college: str, range_filter: str = "weekly", limit: int = 20) -> list[dict]:
    """Get ranked leaderboard for a college."""
    score_col = "weekly_score" if range_filter == "weekly" else "monthly_score"
    db = await get_db()
    try:
        cursor = await db.execute(
            f"""SELECT l.user_id, u.name, u.college,
                       l.{score_col} as points, l.total_interviews
                FROM leaderboard l
                JOIN users u ON u.id = l.user_id
                WHERE l.college = ?
                ORDER BY l.{score_col} DESC
                LIMIT ?""",
            (college, limit)
        )
        rows = await cursor.fetchall()
        result = []
        for i, row in enumerate(rows):
            result.append({
                "rank": i + 1,
                "name": row["name"],
                "department": row["college"],
                "points": row["points"],
                "total_interviews": row["total_interviews"],
            })
        return result
    finally:
        await db.close()


async def get_user_rank(user_id: int, college: str, range_filter: str = "weekly") -> dict:
    """Get the current user's rank within their college."""
    score_col = "weekly_score" if range_filter == "weekly" else "monthly_score"
    db = await get_db()
    try:
        cursor = await db.execute(
            f"""SELECT COUNT(*) + 1 as rank FROM leaderboard
                WHERE college = ? AND {score_col} > (
                    SELECT {score_col} FROM leaderboard WHERE user_id = ?
                )""",
            (college, user_id)
        )
        rank_row = await cursor.fetchone()

        cursor = await db.execute(
            "SELECT COUNT(*) as total FROM leaderboard WHERE college = ?",
            (college,)
        )
        total_row = await cursor.fetchone()

        cursor = await db.execute(
            f"SELECT {score_col} as points FROM leaderboard WHERE user_id = ?",
            (user_id,)
        )
        points_row = await cursor.fetchone()

        return {
            "rank": rank_row["rank"] if rank_row else 0,
            "total": total_row["total"] if total_row else 0,
            "points": points_row["points"] if points_row else 0,
        }
    finally:
        await db.close()
