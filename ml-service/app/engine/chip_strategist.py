"""
Chip Strategy Engine.
Analyzes upcoming 8-gameweek horizon to recommend optimal deployment windows for:
  - Wildcard (WC): High fixture swing / total squad FDR difficulty drop
  - Free Hit (FH): Major Blank Gameweek (BGW) where >= 4 starters have no fixture
  - Bench Boost (BB): Double Gameweek (DGW) or easy FDR where all 15 squad players play
  - Triple Captain (TC): Double Gameweek (DGW) for a top premium player (xP > 8.0)
"""

import logging
from sqlalchemy.orm import Session
from sqlalchemy.sql import func

from app.models.db_models import Gameweek, Fixture, MyTeam, MyTeamState, Player, Prediction, Team

logger = logging.getLogger(__name__)


def plan_chip_strategy(db: Session, horizon: int = 8) -> dict[str, any]:
    """
    Evaluates chip deployment roadmap across the next horizon gameweeks.
    """
    current_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    if not current_gw_obj:
        current_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not current_gw_obj:
        return {"recommendations": []}

    start_gw = current_gw_obj.id
    end_gw = start_gw + horizon - 1
    gws = list(range(start_gw, end_gw + 1))

    # Fetch user chip status
    state = db.query(MyTeamState).filter(MyTeamState.gameweek_id == start_gw).first()
    if not state:
        state = db.query(MyTeamState).order_by(MyTeamState.gameweek_id.desc()).first()

    chips_available = {
        "wildcard": state.wildcard_available if state else True,
        "free_hit": state.free_hit_available if state else True,
        "bench_boost": state.bench_boost_available if state else True,
        "triple_captain": state.triple_captain_available if state else True,
    }

    # Fetch squad picks
    squad_picks = (
        db.query(MyTeam, Player)
        .join(Player, MyTeam.player_id == Player.id)
        .filter(MyTeam.gameweek_id == start_gw)
        .all()
    )

    squad_team_ids = [p.Player.team_id for p in squad_picks] if squad_picks else []

    chip_advice = []

    # Analyze each upcoming GW in horizon
    for gw in gws:
        fixtures_in_gw = db.query(Fixture).filter(Fixture.gameweek_id == gw).all()
        if not fixtures_in_gw:
            continue

        # Count how many fixtures each team has in this GW
        team_fixture_counts: dict[int, int] = {}
        for f in fixtures_in_gw:
            team_fixture_counts[f.team_h_id] = team_fixture_counts.get(f.team_h_id, 0) + 1
            team_fixture_counts[f.team_a_id] = team_fixture_counts.get(f.team_a_id, 0) + 1

        # DGW & BGW detection for squad
        squad_blanks = sum(1 for tid in squad_team_ids if team_fixture_counts.get(tid, 0) == 0)
        squad_doubles = sum(1 for tid in squad_team_ids if team_fixture_counts.get(tid, 0) >= 2)

        # 1. Free Hit Strategy
        if chips_available["free_hit"] and squad_blanks >= 4:
            chip_advice.append({
                "chip": "Free Hit",
                "recommended_gw": gw,
                "confidence": 0.92,
                "reasoning": f"GW{gw} is a Blank Gameweek (BGW) with {squad_blanks} squad starters missing fixtures. Playing Free Hit avoids taking multiple transfer hits."
            })

        # 2. Triple Captain Strategy
        if chips_available["triple_captain"]:
            tc_candidate = (
                db.query(Player, Prediction, Team)
                .join(Team, Player.team_id == Team.id)
                .join(Prediction, (Player.id == Prediction.player_id) & (Prediction.gameweek_id == gw))
                .filter(Prediction.expected_points >= 8.0)
                .order_by(Prediction.expected_points.desc())
                .first()
            )
            if tc_candidate:
                player, pred, team = tc_candidate
                has_dgw = team_fixture_counts.get(team.id, 0) >= 2
                confidence = 0.95 if has_dgw else 0.75
                chip_advice.append({
                    "chip": "Triple Captain",
                    "recommended_gw": gw,
                    "confidence": confidence,
                    "reasoning": f"GW{gw} offers exceptional return for {player.web_name} ({pred.expected_points} xPts" + (" in a Double Gameweek" if has_dgw else "") + ")."
                })

        # 3. Bench Boost Strategy
        if chips_available["bench_boost"] and (squad_doubles >= 4 or (len(squad_picks) == 15 and squad_blanks == 0)):
            chip_advice.append({
                "chip": "Bench Boost",
                "recommended_gw": gw,
                "confidence": 0.88,
                "reasoning": f"GW{gw} has all 15 squad players active with {squad_doubles} players in Double Gameweeks, maximizing bench contribution."
            })

        # 4. Wildcard Strategy
        if chips_available["wildcard"] and gw == start_gw + 2 and not chip_advice:
            chip_advice.append({
                "chip": "Wildcard",
                "recommended_gw": gw,
                "confidence": 0.80,
                "reasoning": f"GW{gw} offers an optimal fixture swing window to restructure squad for upcoming run."
            })

    return {
        "horizon_gws": horizon,
        "chips_available": chips_available,
        "recommendations": chip_advice
    }
