"""
Captaincy Ranker Engine.
Ranks squad starters by risk-adjusted expected points:
  Safe Captain = High xPoints + High Floor (Consistent performers)
  Differential Captain = High Ceiling + Low Ownership (<15%)
"""

import logging
from sqlalchemy.orm import Session

from app.models.db_models import MyTeam, Player, Prediction, Gameweek, Team

logger = logging.getLogger(__name__)


def rank_captains(db: Session) -> dict[str, list[dict]]:
    """
    Ranks captaincy choices from current squad for next GW.
    """
    current_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    if not current_gw_obj:
        current_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not current_gw_obj:
        return {"safe": [], "differential": []}

    target_gw = current_gw_obj.id

    # Fetch starting XI (position <= 11)
    starters = (
        db.query(MyTeam, Player, Prediction, Team)
        .join(Player, MyTeam.player_id == Player.id)
        .join(Team, Player.team_id == Team.id)
        .outerjoin(
            Prediction,
            (Player.id == Prediction.player_id) & (Prediction.gameweek_id == target_gw)
        )
        .filter(MyTeam.gameweek_id == target_gw, MyTeam.position <= 11)
        .all()
    )

    if not starters:
        return {"safe": [], "differential": []}

    candidates = []
    for pick, player, pred, team in starters:
        xp = float(pred.expected_points) if pred and pred.expected_points else float(player.form or 0)
        floor = float(pred.floor) if pred and pred.floor else xp * 0.5
        ceiling = float(pred.ceiling) if pred and pred.ceiling else xp * 1.8
        start_prob = float(pred.start_probability) if pred and pred.start_probability else 0.8
        ownership = float(player.selected_by_percent or 0)

        safe_score = round((xp * 0.6 + floor * 0.4) * start_prob, 2)
        diff_score = round((ceiling * 0.7 + xp * 0.3) * (1.0 - (ownership / 200.0)), 2)

        candidates.append({
            "player_id": player.id,
            "web_name": player.web_name,
            "team_short": team.short_name,
            "expected_points": round(xp, 2),
            "floor": round(floor, 2),
            "ceiling": round(ceiling, 2),
            "start_probability": round(start_prob, 2),
            "ownership_percent": ownership,
            "safe_score": safe_score,
            "differential_score": diff_score,
            "is_current_captain": pick.is_captain or False
        })

    safe_ranked = sorted(candidates, key=lambda c: c["safe_score"], reverse=True)
    diff_ranked = sorted(candidates, key=lambda c: c["differential_score"], reverse=True)

    return {
        "safe": safe_ranked[:5],
        "differential": diff_ranked[:5]
    }
