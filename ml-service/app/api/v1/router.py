from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.ingestion import ingest
from app.models import xpoints_model, backtest
from app.engine import transfer_scorer, captaincy_ranker, weakness_analyzer, differentials, chip_strategist

router = APIRouter()


@router.post("/ingest/full")
async def trigger_full_ingestion(db: Session = Depends(get_db)):
    """Trigger full data ingestion from FPL API into PostgreSQL."""
    try:
        counts = await ingest.run_full_ingestion(db)
        # Auto-trigger baseline predictions for 6-GW horizon after full sync
        xpoints_model.generate_and_save_predictions(db, horizon=6)
        return {"status": "success", "data": counts}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/ingest/team")
async def trigger_team_ingestion(team_id: int = Query(...), db: Session = Depends(get_db)):
    """Sync specific user squad from FPL API and ensure baseline predictions exist."""
    try:
        result = await ingest.sync_user_team(db, team_id)
        # Generate baseline predictions for next 6 GWs
        pred_count = xpoints_model.generate_and_save_predictions(db, horizon=6)
        return {"status": "success", "team_result": result, "predictions_generated": pred_count}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/ingest/history")
async def trigger_history_ingestion(background_tasks: BackgroundTasks, db: Session = Depends(get_db)):
    """Trigger background job to pull historical player stats."""
    background_tasks.add_task(ingest.run_player_history_ingestion, db)
    return {"status": "started", "message": "Player history ingestion running in background"}


@router.get("/predict/status")
def get_predictions_status(db: Session = Depends(get_db)):
    """Returns lightweight prediction model metadata and coverage status."""
    try:
        return xpoints_model.get_prediction_status(db)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/predict/generate")
def generate_predictions(
    target_gw: int = Query(None),
    horizon: int = Query(6, ge=1, le=10),
    db: Session = Depends(get_db)
):
    """Generate xPoints and start probability predictions for a gameweek horizon."""
    try:
        count = xpoints_model.generate_and_save_predictions(db, target_gw, horizon)
        return {"status": "success", "predictions_saved": count, "horizon": horizon}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/evaluation/backtest")
def run_backtest(db: Session = Depends(get_db)):
    """Run model backtesting evaluation against actual player statistics."""
    try:
        metrics = backtest.run_backtest_evaluation(db)
        return {"status": "success", "metrics": metrics}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/transfers")
def get_transfer_recommendations(
    horizon: int = Query(3, ge=1, le=8),
    limit: int = Query(10, ge=1, le=50),
    db: Session = Depends(get_db)
):
    """Get squad-aware, budget-aware transfer recommendations."""
    try:
        recs = transfer_scorer.recommend_transfers(db, horizon, limit)
        return recs
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/captains")
def get_captain_recommendations(
    risk_profile: str = Query("balanced"),
    db: Session = Depends(get_db)
):
    """Get ranked captaincy choices (safe, balanced, aggressive)."""
    try:
        ranks = captaincy_ranker.rank_captains(db, risk_profile)
        return ranks
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/weaknesses")
def get_squad_weaknesses(db: Session = Depends(get_db)):
    """Get squad relative weaknesses, findings, and squad health assessment."""
    try:
        weaknesses = weakness_analyzer.analyze_weaknesses(db)
        return weaknesses
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/differentials")
def get_differentials(
    max_ownership: float = Query(None),
    db: Session = Depends(get_db)
):
    """Get differential targets using stepwise relaxation."""
    try:
        diffs = differentials.detect_differentials(db, max_ownership)
        return diffs
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/chips")
def get_chip_strategy(horizon: int = Query(8, ge=1, le=15), db: Session = Depends(get_db)):
    """Get chip deployment recommendations (Wildcard, Free Hit, Bench Boost, Triple Captain)."""
    try:
        plan = chip_strategist.plan_chip_strategy(db, horizon)
        return {"status": "success", "chip_strategy": plan}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
