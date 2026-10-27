"""
Expected Points (xPoints) Model & Persistence Layer.
Predicts expected points, floor (10th percentile), and ceiling (90th percentile)
conditioned on P(start), position, form, underlying xGI, and fixture difficulty.

Implements transparent heuristic baseline model (baseline-v1) when complex models
are unavailable or uncalibrated, with configurable season scoring rules.
"""

import logging
from datetime import datetime
import pandas as pd
import numpy as np
from sqlalchemy.orm import Session
from sqlalchemy.dialects.postgresql import insert as pg_insert
from sqlalchemy import func, distinct

from app.core.config import settings
from app.models.db_models import Prediction, Gameweek, Player
from app.features.builder import build_feature_dataframe
from app.models.minutes_model import predict_start_probabilities

logger = logging.getLogger(__name__)

MODEL_VERSION = settings.default_model_version


def compute_baseline_xpoints(features_df: pd.DataFrame) -> pd.DataFrame:
    """
    Computes heuristic baseline xPoints using the formula:
    xP = P(start) * [ appearance_pts + per90_attack_rate * (exp_minutes / 90) * fixture_attack_multiplier + CS_prob * cs_pts_by_pos + bonus_expectation ]
    """
    df = features_df.copy()

    # 1. P(start)
    start_probs = predict_start_probabilities(df)
    df['start_prob'] = start_probs

    # 2. Expected minutes if active (default ~75-80 mins for starters, clipped between 45 and 90)
    exp_mins = np.clip(df['rolling_mins_5'].fillna(75.0), 45.0, 90.0)

    # 3. Appearance points: 2 pts if >= 60 mins expected, 1 pt if under 60
    app_pts = np.where(exp_mins >= 60.0, settings.pts_appearance_60_plus, settings.pts_appearance_under_60)

    # 4. per90_attack_rate:
    # Goal points based on element_type (GK/DEF=6, MID=5, FWD=4)
    goal_pts = df['element_type'].map(settings.pts_goal).fillna(4.0)
    assist_pts = settings.pts_assist
    raw_attack_rate = (df['pp90_goals'] * goal_pts) + (df['pp90_assists'] * assist_pts)
    # Blend with xGI per 90 to reward underlying threat even during cold streaks
    xgi_attack_rate = df['pp90_xgi'] * 4.2
    attack_rate = np.maximum(raw_attack_rate, xgi_attack_rate)
    # Form bonus factor (recent momentum)
    form_factor = np.clip(df['rolling_pts_5'] * 0.15, 0.0, 2.5)
    attack_rate = attack_rate + form_factor

    # 5. Fixture difficulty attack multiplier
    fdr_mult = df['fdr'].map(settings.fdr_multipliers).fillna(1.0)
    home_mult = np.where(df['is_home'] == 1, settings.home_multiplier, settings.away_multiplier)
    fixture_attack_mult = fdr_mult * home_mult

    # 6. Clean Sheet Probability (CS_prob) & points
    # FDR 1: 45%, FDR 2: 35%, FDR 3: 25%, FDR 4: 15%, FDR 5: 8%
    cs_prob_base = np.where(df['fdr'] == 1, 0.45,
                  np.where(df['fdr'] == 2, 0.35,
                  np.where(df['fdr'] == 3, 0.25,
                  np.where(df['fdr'] == 4, 0.15, 0.08))))
    cs_prob = cs_prob_base * np.where(df['is_home'] == 1, 1.15, 0.85)
    cs_pts_by_pos = df['element_type'].map(settings.pts_clean_sheet).fillna(0.0)

    # 7. Bonus points expectation
    # Highest for dynamic attackers and playmakers with high xGI / ICT
    bonus_exp = np.clip(df['rolling_xgi_5'] * 0.45 + np.where(df['element_type'].isin([3, 4]), 0.35, 0.15), 0.05, 1.3)

    # Full heuristic xPoints formula
    playing_xpts = (
        app_pts
        + (attack_rate * (exp_mins / 90.0) * fixture_attack_mult)
        + (cs_prob * cs_pts_by_pos)
        + bonus_exp
    )

    # Conditioned on start probability, with small cameo factor (0.5 - 1 pt) if coming off bench
    cameo_prob = (1.0 - start_probs) * (df['chance_of_playing'] / 100.0) * 0.4
    expected_pts = (start_probs * playing_xpts) + (cameo_prob * 1.0)

    # Floor (conservative lower bound) and Ceiling (high-upside potential)
    floor = np.maximum(0.5, expected_pts * 0.45 * start_probs)
    ceiling = expected_pts * 1.85 + np.where(df['element_type'].isin([3, 4]), 3.5, 2.0)

    df['expected_points'] = np.round(expected_pts, 2)
    df['floor'] = np.round(floor, 2)
    df['ceiling'] = np.round(ceiling, 2)
    return df


def generate_and_save_predictions(db: Session, target_gw: int | None = None, horizon: int = 6) -> int:
    """
    Generates xPoints predictions for target_gw (and horizon GWs) and saves them
    into the predictions table. Uses idempotent upsert.
    """
    if target_gw is None:
        next_gw = db.query(Gameweek).filter(Gameweek.is_next == True).first()
        if not next_gw:
            next_gw = db.query(Gameweek).filter(Gameweek.is_current == True).first()
        start_gw = next_gw.id if next_gw else 1
    else:
        start_gw = target_gw

    total_saved = 0
    end_gw = min(39, start_gw + max(1, horizon))

    for gw in range(start_gw, end_gw):
        features_df = build_feature_dataframe(db, gw)
        if features_df.empty:
            continue

        predicted_df = compute_baseline_xpoints(features_df)
        saved_gw_count = 0

        for _, row in predicted_df.iterrows():
            stmt = pg_insert(Prediction).values(
                player_id=int(row['player_id']),
                gameweek_id=gw,
                model_version=MODEL_VERSION,
                expected_points=float(row['expected_points']),
                floor=float(row['floor']),
                ceiling=float(row['ceiling']),
                start_probability=float(row['start_prob']),
            ).on_conflict_do_update(
                index_elements=["player_id", "gameweek_id", "model_version"],
                set_={
                    "expected_points": float(row['expected_points']),
                    "floor": float(row['floor']),
                    "ceiling": float(row['ceiling']),
                    "start_probability": float(row['start_prob']),
                    "created_at": datetime.utcnow(),
                }
            )
            db.execute(stmt)
            saved_gw_count += 1

        db.commit()
        logger.info("Saved %d predictions for GW %d (model: %s)", saved_gw_count, gw, MODEL_VERSION)
        total_saved += saved_gw_count

    return total_saved


def get_prediction_status(db: Session) -> dict:
    """
    Returns prediction status for lightweight reporting:
    { modelVersion, generatedAt, gwCoverage, playerCount }
    """
    row = db.query(
        Prediction.model_version,
        func.max(Prediction.created_at).label("latest_generated"),
        func.count(distinct(Prediction.player_id)).label("player_count"),
        func.min(Prediction.gameweek_id).label("min_gw"),
        func.max(Prediction.gameweek_id).label("max_gw")
    ).filter(Prediction.model_version == MODEL_VERSION).group_by(Prediction.model_version).first()

    if not row or not row.latest_generated:
        # Fallback to any model if default not found
        row = db.query(
            Prediction.model_version,
            func.max(Prediction.created_at).label("latest_generated"),
            func.count(distinct(Prediction.player_id)).label("player_count"),
            func.min(Prediction.gameweek_id).label("min_gw"),
            func.max(Prediction.gameweek_id).label("max_gw")
        ).group_by(Prediction.model_version).order_by(func.max(Prediction.created_at).desc()).first()

    if not row or not row.latest_generated:
        return {
            "modelVersion": MODEL_VERSION,
            "generatedAt": None,
            "gwCoverage": [],
            "playerCount": 0
        }

    coverage = list(range(row.min_gw, row.max_gw + 1)) if row.min_gw and row.max_gw else []

    return {
        "modelVersion": row.model_version,
        "generatedAt": row.latest_generated.isoformat() if hasattr(row.latest_generated, "isoformat") else str(row.latest_generated),
        "gwCoverage": coverage,
        "playerCount": row.player_count or 0
    }
