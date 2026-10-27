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
        return {"status": "success", "data": counts}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/ingest/history")
async def trigger_history_ingestion(background_tasks: BackgroundTasks, db: Session = Depends(get_db)):
    """Trigger background job to pull historical player stats."""
    background_tasks.add_task(ingest.run_player_history_ingestion, db)
    return {"status": "started", "message": "Player history ingestion running in background"}


@router.post("/predict/generate")
def generate_predictions(target_gw: int = Query(None), db: Session = Depends(get_db)):
    """Generate xPoints and start probability predictions for a gameweek."""
    try:
        count = xpoints_model.generate_and_save_predictions(db, target_gw)
        return {"status": "success", "predictions_saved": count}
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
def get_transfer_recommendations(horizon: int = Query(3, ge=1, le=8), db: Session = Depends(get_db)):
    """Get squad-aware, budget-aware transfer recommendations."""
    try:
        recs = transfer_scorer.recommend_transfers(db, horizon)
        return {"status": "success", "horizon": horizon, "recommendations": recs}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/captains")
def get_captain_recommendations(db: Session = Depends(get_db)):
    """Get ranked captaincy choices (safe vs differential)."""
    try:
        ranks = captaincy_ranker.rank_captains(db)
        return {"status": "success", "captaincy_rankings": ranks}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/weaknesses")
def get_squad_weaknesses(db: Session = Depends(get_db)):
    """Get squad starters flagged for low xPoints or injury risk."""
    try:
        weaknesses = weakness_analyzer.analyze_weaknesses(db)
        return {"status": "success", "weaknesses": weaknesses}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommendations/differentials")
def get_differentials(max_ownership: float = Query(12.0), db: Session = Depends(get_db)):
    """Get differential targets (<12% ownership with high xPoints)."""
    try:
        diffs = differentials.detect_differentials(db, max_ownership)
        return {"status": "success", "differentials": diffs}
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
