"""
Data ingestion pipeline: pulls data from the official FPL API and upserts
into Postgres.
"""

import logging
from datetime import datetime
from typing import Any

from sqlalchemy.orm import Session
from sqlalchemy.dialects.postgresql import insert as pg_insert

from app.core.config import settings
from app.ingestion import fpl_client
from app.models.db_models import (
    Team, Player, Gameweek, Fixture, PlayerGameweekStats,
    MyTeam, MyTeamState,
)

logger = logging.getLogger(__name__)


def _parse_dt(iso_str: str | None) -> datetime | None:
    """Parse an ISO datetime string, stripping trailing 'Z'."""
    if not iso_str:
        return None
    try:
        return datetime.fromisoformat(iso_str.replace("Z", "+00:00"))
    except (ValueError, TypeError):
        return None


def _safe_decimal(val: Any) -> float | None:
    """Convert a string or number to float, returning None on failure."""
    if val is None:
        return None
    try:
        return float(val)
    except (ValueError, TypeError):
        return None


async def run_full_ingestion(db: Session) -> dict[str, int]:
    """
    Full ingestion pipeline:
    1. bootstrap-static → teams, players, gameweeks
    2. fixtures
    3. element-summary for players with history (optional, heavy)
    4. user team (if configured)

    Returns counts of upserted records.
    """
    logger.info("Starting full ingestion pipeline…")
    counts = {"teams": 0, "players": 0, "gameweeks": 0, "fixtures": 0, "player_stats": 0}

    # 1. Bootstrap static
    bootstrap = await fpl_client.fetch_bootstrap_static()

    counts["teams"] = _upsert_teams(db, bootstrap.get("teams", []))
    counts["gameweeks"] = _upsert_gameweeks(db, bootstrap.get("events", []))
    counts["players"] = _upsert_players(db, bootstrap.get("elements", []))

    # 2. Fixtures
    fixtures_data = await fpl_client.fetch_fixtures()
    counts["fixtures"] = _upsert_fixtures(db, fixtures_data)

    # 3. User team (if team ID configured)
    team_id = settings.fpl_team_id
    if team_id > 0:
        try:
            await sync_user_team(db, team_id)
        except Exception as e:
            logger.warning("Failed to sync user team (id=%d): %s", team_id, e)

    db.commit()
    logger.info("Full ingestion complete: %s", counts)
    return counts


async def run_player_history_ingestion(db: Session, player_ids: list[int] | None = None) -> int:
    """
    Fetch element-summary for each player and upsert historical GW stats.
    If player_ids is None, fetches for ALL players (slow, ~700 requests).
    """
    if player_ids is None:
        players = db.query(Player.id).all()
        player_ids = [p.id for p in players]

    total = 0
    for i, pid in enumerate(player_ids):
        try:
            summary = await fpl_client.fetch_element_summary(pid)
            history = summary.get("history", [])
            total += _upsert_player_history(db, pid, history)

            if (i + 1) % 50 == 0:
                logger.info("Processed %d / %d player histories", i + 1, len(player_ids))
                db.commit()

        except Exception as e:
            logger.warning("Failed to fetch history for player %d: %s", pid, e)

    db.commit()
    logger.info("Player history ingestion complete: %d stats upserted", total)
    return total


# ─── Upsert helpers ──────────────────────────────────────────────


def _upsert_teams(db: Session, teams: list[dict]) -> int:
    for t in teams:
        stmt = pg_insert(Team).values(
            id=t["id"],
            name=t["name"],
            short_name=t["short_name"],
            code=t.get("code"),
            strength=t.get("strength"),
            strength_overall_home=t.get("strength_overall_home"),
            strength_overall_away=t.get("strength_overall_away"),
            strength_attack_home=t.get("strength_attack_home"),
            strength_attack_away=t.get("strength_attack_away"),
            strength_defence_home=t.get("strength_defence_home"),
            strength_defence_away=t.get("strength_defence_away"),
        ).on_conflict_do_update(
            index_elements=["id"],
            set_={
                "name": t["name"],
                "short_name": t["short_name"],
                "code": t.get("code"),
                "strength": t.get("strength"),
                "strength_overall_home": t.get("strength_overall_home"),
                "strength_overall_away": t.get("strength_overall_away"),
                "strength_attack_home": t.get("strength_attack_home"),
                "strength_attack_away": t.get("strength_attack_away"),
                "strength_defence_home": t.get("strength_defence_home"),
                "strength_defence_away": t.get("strength_defence_away"),
                "updated_at": datetime.utcnow(),
            }
        )
        db.execute(stmt)
    return len(teams)


def _upsert_gameweeks(db: Session, events: list[dict]) -> int:
    for e in events:
        stmt = pg_insert(Gameweek).values(
            id=e["id"],
            name=e.get("name"),
            deadline_time=_parse_dt(e.get("deadline_time")),
            average_entry_score=e.get("average_entry_score"),
            highest_score=e.get("highest_score"),
            is_current=e.get("is_current", False),
            is_next=e.get("is_next", False),
            is_previous=e.get("is_previous", False),
            finished=e.get("finished", False),
            data_checked=e.get("data_checked", False),
        ).on_conflict_do_update(
            index_elements=["id"],
            set_={
                "name": e.get("name"),
                "deadline_time": _parse_dt(e.get("deadline_time")),
                "average_entry_score": e.get("average_entry_score"),
                "highest_score": e.get("highest_score"),
                "is_current": e.get("is_current", False),
                "is_next": e.get("is_next", False),
                "is_previous": e.get("is_previous", False),
                "finished": e.get("finished", False),
                "data_checked": e.get("data_checked", False),
                "updated_at": datetime.utcnow(),
            }
        )
        db.execute(stmt)
    return len(events)


def _upsert_players(db: Session, elements: list[dict]) -> int:
    for el in elements:
        stmt = pg_insert(Player).values(
            id=el["id"],
            team_id=el["team"],
            first_name=el.get("first_name"),
            second_name=el.get("second_name"),
            web_name=el.get("web_name", ""),
            element_type=el.get("element_type"),
            now_cost=el.get("now_cost", 0),
            status=el.get("status"),
            chance_of_playing_next=el.get("chance_of_playing_next_round"),
            news=el.get("news"),
            photo=el.get("photo"),
            form=_safe_decimal(el.get("form")),
            total_points=el.get("total_points", 0),
            points_per_game=_safe_decimal(el.get("points_per_game")),
            selected_by_percent=_safe_decimal(el.get("selected_by_percent")),
            minutes=el.get("minutes", 0),
            goals_scored=el.get("goals_scored", 0),
            assists=el.get("assists", 0),
            clean_sheets=el.get("clean_sheets", 0),
            goals_conceded=el.get("goals_conceded", 0),
            own_goals=el.get("own_goals", 0),
            penalties_saved=el.get("penalties_saved", 0),
            penalties_missed=el.get("penalties_missed", 0),
            yellow_cards=el.get("yellow_cards", 0),
            red_cards=el.get("red_cards", 0),
            saves=el.get("saves", 0),
            bonus=el.get("bonus", 0),
            bps=el.get("bps", 0),
            influence=_safe_decimal(el.get("influence")),
            creativity=_safe_decimal(el.get("creativity")),
            threat=_safe_decimal(el.get("threat")),
            ict_index=_safe_decimal(el.get("ict_index")),
            expected_goals=_safe_decimal(el.get("expected_goals")),
            expected_assists=_safe_decimal(el.get("expected_assists")),
            expected_goal_involvements=_safe_decimal(el.get("expected_goal_involvements")),
            expected_goals_conceded=_safe_decimal(el.get("expected_goals_conceded")),
            transfers_in_event=el.get("transfers_in_event", 0),
            transfers_out_event=el.get("transfers_out_event", 0),
        ).on_conflict_do_update(
            index_elements=["id"],
            set_={
                "team_id": el["team"],
                "first_name": el.get("first_name"),
                "second_name": el.get("second_name"),
                "web_name": el.get("web_name", ""),
                "element_type": el.get("element_type"),
                "now_cost": el.get("now_cost", 0),
                "status": el.get("status"),
                "chance_of_playing_next": el.get("chance_of_playing_next_round"),
                "news": el.get("news"),
                "photo": el.get("photo"),
                "form": _safe_decimal(el.get("form")),
                "total_points": el.get("total_points", 0),
                "points_per_game": _safe_decimal(el.get("points_per_game")),
                "selected_by_percent": _safe_decimal(el.get("selected_by_percent")),
                "minutes": el.get("minutes", 0),
                "goals_scored": el.get("goals_scored", 0),
                "assists": el.get("assists", 0),
                "clean_sheets": el.get("clean_sheets", 0),
                "goals_conceded": el.get("goals_conceded", 0),
                "own_goals": el.get("own_goals", 0),
                "penalties_saved": el.get("penalties_saved", 0),
                "penalties_missed": el.get("penalties_missed", 0),
                "yellow_cards": el.get("yellow_cards", 0),
                "red_cards": el.get("red_cards", 0),
                "saves": el.get("saves", 0),
                "bonus": el.get("bonus", 0),
                "bps": el.get("bps", 0),
                "influence": _safe_decimal(el.get("influence")),
                "creativity": _safe_decimal(el.get("creativity")),
                "threat": _safe_decimal(el.get("threat")),
                "ict_index": _safe_decimal(el.get("ict_index")),
                "expected_goals": _safe_decimal(el.get("expected_goals")),
                "expected_assists": _safe_decimal(el.get("expected_assists")),
                "expected_goal_involvements": _safe_decimal(el.get("expected_goal_involvements")),
                "expected_goals_conceded": _safe_decimal(el.get("expected_goals_conceded")),
                "transfers_in_event": el.get("transfers_in_event", 0),
                "transfers_out_event": el.get("transfers_out_event", 0),
                "updated_at": datetime.utcnow(),
            }
        )
        db.execute(stmt)
    return len(elements)


def _upsert_fixtures(db: Session, fixtures: list[dict]) -> int:
    for f in fixtures:
        gw_id = f.get("event")  # can be None for unscheduled fixtures
        stmt = pg_insert(Fixture).values(
            id=f["id"],
            gameweek_id=gw_id,
            team_h_id=f["team_h"],
            team_a_id=f["team_a"],
            team_h_difficulty=f.get("team_h_difficulty"),
            team_a_difficulty=f.get("team_a_difficulty"),
            kickoff_time=_parse_dt(f.get("kickoff_time")),
            finished=f.get("finished", False),
            team_h_score=f.get("team_h_score"),
            team_a_score=f.get("team_a_score"),
        ).on_conflict_do_update(
            index_elements=["id"],
            set_={
                "gameweek_id": gw_id,
                "team_h_id": f["team_h"],
                "team_a_id": f["team_a"],
                "team_h_difficulty": f.get("team_h_difficulty"),
                "team_a_difficulty": f.get("team_a_difficulty"),
                "kickoff_time": _parse_dt(f.get("kickoff_time")),
                "finished": f.get("finished", False),
                "team_h_score": f.get("team_h_score"),
                "team_a_score": f.get("team_a_score"),
                "updated_at": datetime.utcnow(),
            }
        )
        db.execute(stmt)
    return len(fixtures)


def _upsert_player_history(db: Session, player_id: int, history: list[dict]) -> int:
    count = 0
    for h in history:
        gw_id = h.get("round")
        fixture_id = h.get("fixture")

        stmt = pg_insert(PlayerGameweekStats).values(
            player_id=player_id,
            gameweek_id=gw_id,
            fixture_id=fixture_id,
            total_points=h.get("total_points", 0),
            minutes=h.get("minutes", 0),
            goals_scored=h.get("goals_scored", 0),
            assists=h.get("assists", 0),
            clean_sheets=h.get("clean_sheets", 0),
            goals_conceded=h.get("goals_conceded", 0),
            own_goals=h.get("own_goals", 0),
            penalties_saved=h.get("penalties_saved", 0),
            penalties_missed=h.get("penalties_missed", 0),
            yellow_cards=h.get("yellow_cards", 0),
            red_cards=h.get("red_cards", 0),
            saves=h.get("saves", 0),
            bonus=h.get("bonus", 0),
            bps=h.get("bps", 0),
            influence=_safe_decimal(h.get("influence")),
            creativity=_safe_decimal(h.get("creativity")),
            threat=_safe_decimal(h.get("threat")),
            ict_index=_safe_decimal(h.get("ict_index")),
            expected_goals=_safe_decimal(h.get("expected_goals")),
            expected_assists=_safe_decimal(h.get("expected_assists")),
            expected_goal_involvements=_safe_decimal(h.get("expected_goal_involvements")),
            expected_goals_conceded=_safe_decimal(h.get("expected_goals_conceded")),
            value=h.get("value"),
            selected=h.get("selected"),
            transfers_in=h.get("transfers_in", 0),
            transfers_out=h.get("transfers_out", 0),
        ).on_conflict_do_update(
            index_elements=["player_id", "gameweek_id", "fixture_id"],
            set_={
                "total_points": h.get("total_points", 0),
                "minutes": h.get("minutes", 0),
                "goals_scored": h.get("goals_scored", 0),
                "assists": h.get("assists", 0),
                "clean_sheets": h.get("clean_sheets", 0),
                "goals_conceded": h.get("goals_conceded", 0),
                "own_goals": h.get("own_goals", 0),
                "penalties_saved": h.get("penalties_saved", 0),
                "penalties_missed": h.get("penalties_missed", 0),
                "yellow_cards": h.get("yellow_cards", 0),
                "red_cards": h.get("red_cards", 0),
                "saves": h.get("saves", 0),
                "bonus": h.get("bonus", 0),
                "bps": h.get("bps", 0),
                "influence": _safe_decimal(h.get("influence")),
                "creativity": _safe_decimal(h.get("creativity")),
                "threat": _safe_decimal(h.get("threat")),
                "ict_index": _safe_decimal(h.get("ict_index")),
                "expected_goals": _safe_decimal(h.get("expected_goals")),
                "expected_assists": _safe_decimal(h.get("expected_assists")),
                "expected_goal_involvements": _safe_decimal(h.get("expected_goal_involvements")),
                "expected_goals_conceded": _safe_decimal(h.get("expected_goals_conceded")),
                "value": h.get("value"),
                "selected": h.get("selected"),
                "transfers_in": h.get("transfers_in", 0),
                "transfers_out": h.get("transfers_out", 0),
                "updated_at": datetime.utcnow(),
            }
        )
        db.execute(stmt)
        count += 1
    return count


async def sync_user_team(db: Session, team_id: int) -> dict:
    """Sync user's team state and picks from the FPL API."""
    entry = await fpl_client.fetch_entry(team_id)
    current_event = entry.get("current_event")

    if not current_event:
        logger.warning("No current event for team %d", team_id)
        return {"status": "warning", "message": "No current event"}

    # Upsert team state
    stmt = pg_insert(MyTeamState).values(
        gameweek_id=current_event,
        overall_rank=entry.get("summary_overall_rank"),
        overall_points=entry.get("summary_overall_points"),
        gameweek_points=entry.get("summary_event_points"),
        bank=entry.get("last_deadline_bank"),
        team_value=entry.get("last_deadline_value"),
    ).on_conflict_do_update(
        index_elements=["gameweek_id"],
        set_={
            "overall_rank": entry.get("summary_overall_rank"),
            "overall_points": entry.get("summary_overall_points"),
            "gameweek_points": entry.get("summary_event_points"),
            "bank": entry.get("last_deadline_bank"),
            "team_value": entry.get("last_deadline_value"),
            "updated_at": datetime.utcnow(),
        }
    )
    db.execute(stmt)

    # Fetch picks
    picks_count = 0
    try:
        picks_data = await fpl_client.fetch_picks(team_id, current_event)
        picks = picks_data.get("picks", [])

        # Delete existing picks for this GW
        db.query(MyTeam).filter(MyTeam.gameweek_id == current_event).delete()

        for p in picks:
            pick = MyTeam(
                gameweek_id=current_event,
                player_id=p["element"],
                position=p["position"],
                multiplier=p["multiplier"],
                is_captain=p.get("is_captain", False),
                is_vice_captain=p.get("is_vice_captain", False),
            )
            db.add(pick)

        picks_count = len(picks)
        logger.info("Synced %d picks for GW %d", picks_count, current_event)
    except Exception as e:
        logger.warning("Could not fetch picks for GW %d: %s", current_event, e)

    db.commit()
    return {"gameweek_id": current_event, "picks_count": picks_count}
