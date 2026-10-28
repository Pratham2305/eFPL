-- =====================================================================
-- V4: FPL Assistant — Season Performance History & Gameweek Tracking
-- =====================================================================

-- ─── Gameweeks (ensure columns exist) ──────────────────────────
ALTER TABLE gameweeks ADD COLUMN IF NOT EXISTS average_entry_score INTEGER;
ALTER TABLE gameweeks ADD COLUMN IF NOT EXISTS highest_score INTEGER;
ALTER TABLE gameweeks ADD COLUMN IF NOT EXISTS finished BOOLEAN DEFAULT FALSE;
ALTER TABLE gameweeks ADD COLUMN IF NOT EXISTS data_checked BOOLEAN DEFAULT FALSE;

-- ─── My Team State (historical stats per GW) ───────────────────
ALTER TABLE my_team_state ADD COLUMN IF NOT EXISTS gameweek_rank INTEGER;
ALTER TABLE my_team_state ADD COLUMN IF NOT EXISTS event_transfers INTEGER;
ALTER TABLE my_team_state ADD COLUMN IF NOT EXISTS event_transfers_cost INTEGER;
ALTER TABLE my_team_state ADD COLUMN IF NOT EXISTS points_on_bench INTEGER;
ALTER TABLE my_team_state ADD COLUMN IF NOT EXISTS active_chip VARCHAR(30);

-- ─── My Team Auto Subs (new) ───────────────────────────────────
CREATE TABLE IF NOT EXISTS my_team_auto_subs (
    id                      SERIAL PRIMARY KEY,
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    element_in              INTEGER NOT NULL REFERENCES players(id),
    element_out             INTEGER NOT NULL REFERENCES players(id),
    created_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(gameweek_id, element_in, element_out)
);

CREATE INDEX IF NOT EXISTS idx_autosubs_gw ON my_team_auto_subs(gameweek_id);

-- ─── My Team Picks (unique constraint for re-sync upserts) ─────
CREATE UNIQUE INDEX IF NOT EXISTS idx_myteam_gw_player ON my_team(gameweek_id, player_id);

-- ─── Player Gameweek Stats (live/event actuals) ────────────────
ALTER TABLE player_gameweek_stats ADD COLUMN IF NOT EXISTS starts INTEGER DEFAULT 0;
ALTER TABLE player_gameweek_stats ADD COLUMN IF NOT EXISTS defensive_contribution INTEGER DEFAULT 0;
ALTER TABLE player_gameweek_stats ADD COLUMN IF NOT EXISTS recoveries INTEGER DEFAULT 0;
ALTER TABLE player_gameweek_stats ADD COLUMN IF NOT EXISTS tackles INTEGER DEFAULT 0;
ALTER TABLE player_gameweek_stats ADD COLUMN IF NOT EXISTS clearances_blocks_interceptions INTEGER DEFAULT 0;
ALTER TABLE player_gameweek_stats ADD COLUMN IF NOT EXISTS in_dreamteam BOOLEAN DEFAULT FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_pgs_player_gw_unique ON player_gameweek_stats(player_id, gameweek_id);

-- ─── Transfers Log (idempotency key & entry) ───────────────────
ALTER TABLE transfers_log ADD COLUMN IF NOT EXISTS entry_id INTEGER;
CREATE UNIQUE INDEX IF NOT EXISTS idx_transfers_unique ON transfers_log(gameweek_id, player_in_id, player_out_id, transfer_time);
