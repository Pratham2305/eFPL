-- =====================================================================
-- V1: FPL Assistant — Initial Schema
-- =====================================================================

-- ─── EPL Clubs ────────────────────────────────────────────────
CREATE TABLE teams (
    id                      INTEGER PRIMARY KEY,          -- FPL API team id
    name                    VARCHAR(100) NOT NULL,
    short_name              VARCHAR(10)  NOT NULL,
    code                    INTEGER,
    strength                INTEGER,
    strength_overall_home   INTEGER,
    strength_overall_away   INTEGER,
    strength_attack_home    INTEGER,
    strength_attack_away    INTEGER,
    strength_defence_home   INTEGER,
    strength_defence_away   INTEGER,
    updated_at              TIMESTAMP DEFAULT NOW()
);

-- ─── FPL Players ──────────────────────────────────────────────
CREATE TABLE players (
    id                      INTEGER PRIMARY KEY,          -- FPL API element id
    team_id                 INTEGER NOT NULL REFERENCES teams(id),
    first_name              VARCHAR(100),
    second_name             VARCHAR(100),
    web_name                VARCHAR(100) NOT NULL,
    element_type            SMALLINT NOT NULL,             -- 1=GK, 2=DEF, 3=MID, 4=FWD
    now_cost                INTEGER NOT NULL,              -- price in tenths (e.g. 100 = £10.0m)
    status                  VARCHAR(5),                    -- a, d, i, s, u, n
    chance_of_playing_next  INTEGER,                       -- 0-100 or null
    news                    TEXT,
    photo                   VARCHAR(100),
    form                    NUMERIC(5,2),
    total_points            INTEGER DEFAULT 0,
    points_per_game         NUMERIC(5,2),
    selected_by_percent     NUMERIC(6,2),
    minutes                 INTEGER DEFAULT 0,
    goals_scored            INTEGER DEFAULT 0,
    assists                 INTEGER DEFAULT 0,
    clean_sheets            INTEGER DEFAULT 0,
    goals_conceded          INTEGER DEFAULT 0,
    own_goals               INTEGER DEFAULT 0,
    penalties_saved         INTEGER DEFAULT 0,
    penalties_missed        INTEGER DEFAULT 0,
    yellow_cards            INTEGER DEFAULT 0,
    red_cards               INTEGER DEFAULT 0,
    saves                   INTEGER DEFAULT 0,
    bonus                   INTEGER DEFAULT 0,
    bps                     INTEGER DEFAULT 0,
    influence               NUMERIC(7,2),
    creativity              NUMERIC(7,2),
    threat                  NUMERIC(7,2),
    ict_index               NUMERIC(7,2),
    expected_goals          NUMERIC(7,3),
    expected_assists        NUMERIC(7,3),
    expected_goal_involvements NUMERIC(7,3),
    expected_goals_conceded NUMERIC(7,3),
    transfers_in_event      INTEGER DEFAULT 0,
    transfers_out_event     INTEGER DEFAULT 0,
    updated_at              TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_players_team ON players(team_id);
CREATE INDEX idx_players_element_type ON players(element_type);

-- ─── Gameweeks ────────────────────────────────────────────────
CREATE TABLE gameweeks (
    id                      INTEGER PRIMARY KEY,          -- GW number (1-38)
    name                    VARCHAR(50),
    deadline_time           TIMESTAMP,
    average_entry_score     INTEGER,
    highest_score           INTEGER,
    is_current              BOOLEAN DEFAULT FALSE,
    is_next                 BOOLEAN DEFAULT FALSE,
    is_previous             BOOLEAN DEFAULT FALSE,
    finished                BOOLEAN DEFAULT FALSE,
    data_checked            BOOLEAN DEFAULT FALSE,
    updated_at              TIMESTAMP DEFAULT NOW()
);

-- ─── Fixtures ─────────────────────────────────────────────────
CREATE TABLE fixtures (
    id                      INTEGER PRIMARY KEY,          -- FPL API fixture id
    gameweek_id             INTEGER REFERENCES gameweeks(id),
    team_h_id               INTEGER NOT NULL REFERENCES teams(id),
    team_a_id               INTEGER NOT NULL REFERENCES teams(id),
    team_h_difficulty       SMALLINT,                     -- FDR 1-5
    team_a_difficulty       SMALLINT,                     -- FDR 1-5
    kickoff_time            TIMESTAMP,
    finished                BOOLEAN DEFAULT FALSE,
    team_h_score            INTEGER,
    team_a_score            INTEGER,
    updated_at              TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_fixtures_gameweek ON fixtures(gameweek_id);
CREATE INDEX idx_fixtures_team_h ON fixtures(team_h_id);
CREATE INDEX idx_fixtures_team_a ON fixtures(team_a_id);

-- ─── Per-Player Per-GW Stats ──────────────────────────────────
CREATE TABLE player_gameweek_stats (
    id                      SERIAL PRIMARY KEY,
    player_id               INTEGER NOT NULL REFERENCES players(id),
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    fixture_id              INTEGER REFERENCES fixtures(id),
    total_points            INTEGER DEFAULT 0,
    minutes                 INTEGER DEFAULT 0,
    goals_scored            INTEGER DEFAULT 0,
    assists                 INTEGER DEFAULT 0,
    clean_sheets            INTEGER DEFAULT 0,
    goals_conceded          INTEGER DEFAULT 0,
    own_goals               INTEGER DEFAULT 0,
    penalties_saved         INTEGER DEFAULT 0,
    penalties_missed        INTEGER DEFAULT 0,
    yellow_cards            INTEGER DEFAULT 0,
    red_cards               INTEGER DEFAULT 0,
    saves                   INTEGER DEFAULT 0,
    bonus                   INTEGER DEFAULT 0,
    bps                     INTEGER DEFAULT 0,
    influence               NUMERIC(7,2),
    creativity              NUMERIC(7,2),
    threat                  NUMERIC(7,2),
    ict_index               NUMERIC(7,2),
    expected_goals          NUMERIC(7,3),
    expected_assists        NUMERIC(7,3),
    expected_goal_involvements NUMERIC(7,3),
    expected_goals_conceded NUMERIC(7,3),
    value                   INTEGER,                       -- player price at that GW
    selected                INTEGER,                       -- ownership count
    transfers_in            INTEGER DEFAULT 0,
    transfers_out           INTEGER DEFAULT 0,
    updated_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(player_id, gameweek_id, fixture_id)
);

CREATE INDEX idx_pgs_player ON player_gameweek_stats(player_id);
CREATE INDEX idx_pgs_gameweek ON player_gameweek_stats(gameweek_id);
CREATE INDEX idx_pgs_player_gw ON player_gameweek_stats(player_id, gameweek_id);

-- ─── My Team Picks (per GW) ──────────────────────────────────
CREATE TABLE my_team (
    id                      SERIAL PRIMARY KEY,
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    player_id               INTEGER NOT NULL REFERENCES players(id),
    position                SMALLINT NOT NULL,             -- 1-15 (1-11 = XI, 12-15 = bench)
    multiplier              SMALLINT NOT NULL DEFAULT 1,   -- 0=bench-not-playing, 1=playing, 2=captain, 3=TC
    is_captain              BOOLEAN DEFAULT FALSE,
    is_vice_captain         BOOLEAN DEFAULT FALSE,
    updated_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(gameweek_id, position)
);

CREATE INDEX idx_myteam_gameweek ON my_team(gameweek_id);

-- ─── My Team State (overall) ─────────────────────────────────
CREATE TABLE my_team_state (
    id                      SERIAL PRIMARY KEY,
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    overall_rank            INTEGER,
    overall_points          INTEGER,
    gameweek_points         INTEGER,
    bank                    INTEGER DEFAULT 0,             -- in tenths (e.g. 15 = £1.5m)
    team_value              INTEGER DEFAULT 0,             -- in tenths
    free_transfers          INTEGER DEFAULT 1,
    wildcard_available      BOOLEAN DEFAULT TRUE,
    free_hit_available      BOOLEAN DEFAULT TRUE,
    bench_boost_available   BOOLEAN DEFAULT TRUE,
    triple_captain_available BOOLEAN DEFAULT TRUE,
    updated_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(gameweek_id)
);

-- ─── Transfer Log ─────────────────────────────────────────────
CREATE TABLE transfers_log (
    id                      SERIAL PRIMARY KEY,
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    player_in_id            INTEGER NOT NULL REFERENCES players(id),
    player_out_id           INTEGER NOT NULL REFERENCES players(id),
    price_in                INTEGER,                       -- in tenths
    price_out               INTEGER,                       -- in tenths
    transfer_time           TIMESTAMP DEFAULT NOW(),
    updated_at              TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_transfers_gameweek ON transfers_log(gameweek_id);

-- ─── Predictions (Phase 4) ───────────────────────────────────
CREATE TABLE predictions (
    id                      SERIAL PRIMARY KEY,
    player_id               INTEGER NOT NULL REFERENCES players(id),
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    model_version           VARCHAR(50) NOT NULL,
    expected_points         NUMERIC(6,2),
    floor                   NUMERIC(6,2),
    ceiling                 NUMERIC(6,2),
    start_probability       NUMERIC(4,3),
    created_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(player_id, gameweek_id, model_version)
);

CREATE INDEX idx_predictions_player_gw ON predictions(player_id, gameweek_id);

-- ─── Recommendations (Phase 5) ───────────────────────────────
CREATE TABLE recommendations (
    id                      SERIAL PRIMARY KEY,
    gameweek_id             INTEGER NOT NULL REFERENCES gameweeks(id),
    type                    VARCHAR(30) NOT NULL,          -- transfer, captain, chip
    payload                 JSONB,
    reasoning               TEXT,
    confidence              NUMERIC(4,3),
    created_at              TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_recommendations_gw ON recommendations(gameweek_id);
CREATE INDEX idx_recommendations_type ON recommendations(type);
