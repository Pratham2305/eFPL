import pytest
import pandas as pd
from app.core.database import SessionLocal
from app.models.xpoints_model import compute_baseline_xpoints, get_prediction_status
from app.engine.transfer_scorer import recommend_transfers
from app.engine.captaincy_ranker import rank_captains
from app.engine.weakness_analyzer import analyze_weaknesses
from app.engine.differentials import detect_differentials


def test_compute_baseline_xpoints():
    df = pd.DataFrame([{
        "player_id": 1,
        "web_name": "TestPlayer",
        "element_type": 3,  # MID
        "now_cost": 80,
        "chance_of_playing": 100,
        "selected_by_percent": 15.0,
        "target_gw": 9,
        "is_home": 1,
        "fdr": 2,
        "opp_def_strength": 1000,
        "rolling_pts_3": 5.0,
        "rolling_pts_5": 5.0,
        "rolling_mins_3": 85.0,
        "rolling_mins_5": 85.0,
        "rolling_xg_5": 0.4,
        "rolling_xa_5": 0.3,
        "rolling_xgi_5": 0.7,
        "start_ratio_5": 1.0,
        "pp90_goals": 0.3,
        "pp90_assists": 0.3,
        "pp90_xgi": 0.6,
    }])

    result_df = compute_baseline_xpoints(df)
    assert not result_df.empty
    assert "expected_points" in result_df.columns
    assert "floor" in result_df.columns
    assert "ceiling" in result_df.columns
    assert "start_prob" in result_df.columns
    xp = result_df["expected_points"].iloc[0]
    assert 2.0 <= xp <= 12.0
    assert result_df["floor"].iloc[0] <= xp
    assert result_df["ceiling"].iloc[0] >= xp


def test_prediction_status():
    db = SessionLocal()
    try:
        status = get_prediction_status(db)
        assert "modelVersion" in status
        assert "gwCoverage" in status
        assert "playerCount" in status
    finally:
        db.close()


def test_transfer_scorer():
    db = SessionLocal()
    try:
        result = recommend_transfers(db, horizon=3, limit=5)
        assert result["status"] == "success"
        assert len(result["recommendations"]) > 0
        rec = result["recommendations"][0]
        assert rec["verdict"] in ["RECOMMENDED", "CONSIDER", "SIDEWAYS", "NOT_WORTH_HIT"]
        assert "reasons" in rec
        assert len(rec["reasons"]) > 0
    finally:
        db.close()


def test_captaincy_ranker():
    db = SessionLocal()
    try:
        for profile in ["balanced", "safe", "aggressive"]:
            res = rank_captains(db, risk_profile=profile)
            assert res["status"] == "success"
            assert len(res["rankings"]) == 11
            assert res["top_recommendation"] is not None
            assert res["vice_captain_suggestion"] is not None
            assert len(res["outside_squad"]) > 0
    finally:
        db.close()


def test_weakness_analyzer():
    db = SessionLocal()
    try:
        res = analyze_weaknesses(db)
        assert res["status"] == "success"
        assert "squadHealth" in res
        assert 0 <= res["squadHealth"]["score"] <= 100
        # Guarantees at least 5 findings
        assert len(res["findings"]) >= 5
        finding = res["findings"][0]
        assert finding["severity"] in ["CRITICAL", "MODERATE", "MINOR", "OPPORTUNITY"]
        assert len(finding["evidence"]) > 0
    finally:
        db.close()


def test_differentials():
    db = SessionLocal()
    try:
        res = detect_differentials(db)
        assert res["status"] == "success"
        assert "threshold_used" in res
        assert len(res["differentials"]) >= 5
    finally:
        db.close()
