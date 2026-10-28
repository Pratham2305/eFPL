package com.fplassistant.dto;

public class SyncResponse {
    private String status;
    private String message;
    private Integer playersUpdated;
    private Integer fixturesUpdated;
    private Integer gameweeksUpdated;
    private Integer historyGameweeksUpdated;
    private Integer picksUpdated;
    private Integer statsUpdated;
    private Integer transfersUpdated;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getPlayersUpdated() { return playersUpdated; }
    public void setPlayersUpdated(Integer playersUpdated) { this.playersUpdated = playersUpdated; }

    public Integer getFixturesUpdated() { return fixturesUpdated; }
    public void setFixturesUpdated(Integer fixturesUpdated) { this.fixturesUpdated = fixturesUpdated; }

    public Integer getGameweeksUpdated() { return gameweeksUpdated; }
    public void setGameweeksUpdated(Integer gameweeksUpdated) { this.gameweeksUpdated = gameweeksUpdated; }

    public Integer getHistoryGameweeksUpdated() { return historyGameweeksUpdated; }
    public void setHistoryGameweeksUpdated(Integer historyGameweeksUpdated) { this.historyGameweeksUpdated = historyGameweeksUpdated; }

    public Integer getPicksUpdated() { return picksUpdated; }
    public void setPicksUpdated(Integer picksUpdated) { this.picksUpdated = picksUpdated; }

    public Integer getStatsUpdated() { return statsUpdated; }
    public void setStatsUpdated(Integer statsUpdated) { this.statsUpdated = statsUpdated; }

    public Integer getTransfersUpdated() { return transfersUpdated; }
    public void setTransfersUpdated(Integer transfersUpdated) { this.transfersUpdated = transfersUpdated; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SyncResponse instance = new SyncResponse();
        public Builder status(String v) { instance.setStatus(v); return this; }
        public Builder message(String v) { instance.setMessage(v); return this; }
        public Builder playersUpdated(Integer v) { instance.setPlayersUpdated(v); return this; }
        public Builder fixturesUpdated(Integer v) { instance.setFixturesUpdated(v); return this; }
        public Builder gameweeksUpdated(Integer v) { instance.setGameweeksUpdated(v); return this; }
        public Builder historyGameweeksUpdated(Integer v) { instance.setHistoryGameweeksUpdated(v); return this; }
        public Builder picksUpdated(Integer v) { instance.setPicksUpdated(v); return this; }
        public Builder statsUpdated(Integer v) { instance.setStatsUpdated(v); return this; }
        public Builder transfersUpdated(Integer v) { instance.setTransfersUpdated(v); return this; }
        public SyncResponse build() { return instance; }
    }
}
