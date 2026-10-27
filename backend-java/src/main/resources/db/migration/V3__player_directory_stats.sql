-- =====================================================================
-- V3: Player Directory Extended Statistics
-- =====================================================================

ALTER TABLE players ADD COLUMN chance_of_playing_this_round INTEGER;
ALTER TABLE players ADD COLUMN news_added TIMESTAMP;
ALTER TABLE players ADD COLUMN starts INTEGER DEFAULT 0;
ALTER TABLE players ADD COLUMN cost_change_start INTEGER DEFAULT 0;
ALTER TABLE players ADD COLUMN cost_change_event INTEGER DEFAULT 0;
ALTER TABLE players ADD COLUMN ep_next NUMERIC(5,2);
