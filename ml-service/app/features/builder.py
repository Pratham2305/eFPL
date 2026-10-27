"""
Feature Engineering Pipeline: Extracts tabular feature DataFrames for player-gameweek
prediction tasks using historical stats, player info, and fixture data.
"""

import logging
import pandas as pd
import numpy as np
from sqlalchemy.orm import Session

from app.models.db_models import Player, PlayerGameweekStats, Fixture, Team, Gameweek

logger = logging.getLogger(__name__)


def build_feature_dataframe(db: Session, target_gw: int | None = None) -> pd.DataFrame:
    """
    Extracts features for all active players for a target gameweek.
    If target_gw is None, uses the current/next gameweek.
    """
    if target_gw is None:
        next_gw = db.query(Gameweek).filter(Gameweek.is_next == True).first()
        target_gw = next_gw.id if next_gw else 1

    logger.info("Building features for GW %d", target_gw)

    # 1. Fetch active players and their team info
    players = db.query(Player, Team).join(Team, Player.team_id == Team.id).filter(Player.status != 'u').all()
    if not players:
        logger.warning("No players found in database to build features.")
        return pd.DataFrame()

    player_data = []
    for p, t in players:
        # Fetch historical stats prior to target_gw
        history = (
            db.query(PlayerGameweekStats)
            .filter(PlayerGameweekStats.player_id == p.id, PlayerGameweekStats.gameweek_id < target_gw)
            .order_by(PlayerGameweekStats.gameweek_id.desc())
            .all()
        )

        # Get upcoming fixture for target_gw
        fixture = (
            db.query(Fixture)
            .filter(
                (Fixture.gameweek_id == target_gw) &
                ((Fixture.team_h_id == t.id) | (Fixture.team_a_id == t.id))
            )
            .first()
        )

        is_home = fixture.team_h_id == t.id if fixture else True
        fdr = (fixture.team_h_difficulty if is_home else fixture.team_a_difficulty) if fixture else 3
        opp_id = (fixture.team_a_id if is_home else fixture.team_h_id) if fixture else None

        opp_team = db.query(Team).filter(Team.id == opp_id).first() if opp_id else None
        opp_def_strength = (opp_team.strength_defence_away if is_home else opp_team.strength_defence_home) if opp_team else 1000

        # Extract rolling features
        hist_df = pd.DataFrame([{
            'gw': h.gameweek_id,
            'minutes': h.minutes or 0,
            'points': h.total_points or 0,
            'goals': h.goals_scored or 0,
            'assists': h.assists or 0,
            'xg': float(h.expected_goals or 0),
            'xa': float(h.expected_assists or 0),
            'xgi': float(h.expected_goal_involvements or 0),
            'ict': float(h.ict_index or 0),
            'value': h.value or p.now_cost,
        } for h in history])

        if not hist_df.empty:
            last_3 = hist_df.head(3)
            last_5 = hist_df.head(5)

            rolling_pts_3 = last_3['points'].mean()
            rolling_pts_5 = last_5['points'].mean()
            rolling_mins_3 = last_3['minutes'].mean()
            rolling_mins_5 = last_5['minutes'].mean()
            rolling_xg_5 = last_5['xg'].mean()
            rolling_xa_5 = last_5['xa'].mean()
            rolling_xgi_5 = last_5['xgi'].mean()

            starts_last_5 = (last_5['minutes'] >= 60).sum()
            start_ratio_5 = starts_last_5 / len(last_5)
        else:
            rolling_pts_3 = float(p.form or 0)
            rolling_pts_5 = float(p.form or 0)
            rolling_mins_3 = 60.0 if p.minutes > 0 else 0.0
            rolling_mins_5 = 60.0 if p.minutes > 0 else 0.0
            rolling_xg_5 = float(p.expected_goals or 0) / max(1, p.minutes / 90.0)
            rolling_xa_5 = float(p.expected_assists or 0) / max(1, p.minutes / 90.0)
            rolling_xgi_5 = float(p.expected_goal_involvements or 0) / max(1, p.minutes / 90.0)
            start_ratio_5 = 1.0 if p.minutes > 300 else 0.5

        # Per 90 rates
        tot_mins = max(1, p.minutes)
        pp90_goals = (p.goals_scored / tot_mins) * 90.0
        pp90_assists = (p.assists / tot_mins) * 90.0
        pp90_xgi = (float(p.expected_goal_involvements or 0) / tot_mins) * 90.0

        player_data.append({
            'player_id': p.id,
            'web_name': p.web_name,
            'element_type': p.element_type,
            'now_cost': p.now_cost,
            'chance_of_playing': p.chance_of_playing_next if p.chance_of_playing_next is not None else 100,
            'selected_by_percent': float(p.selected_by_percent or 0),
            'target_gw': target_gw,
            'is_home': 1 if is_home else 0,
            'fdr': fdr,
            'opp_def_strength': opp_def_strength,
            'rolling_pts_3': rolling_pts_3,
            'rolling_pts_5': rolling_pts_5,
            'rolling_mins_3': rolling_mins_3,
            'rolling_mins_5': rolling_mins_5,
            'rolling_xg_5': rolling_xg_5,
            'rolling_xa_5': rolling_xa_5,
            'rolling_xgi_5': rolling_xgi_5,
            'start_ratio_5': start_ratio_5,
            'pp90_goals': pp90_goals,
            'pp90_assists': pp90_assists,
            'pp90_xgi': pp90_xgi,
        })

    df = pd.DataFrame(player_data)
    logger.info("Extracted %d player features for GW %d", len(df), target_gw)
    return df
