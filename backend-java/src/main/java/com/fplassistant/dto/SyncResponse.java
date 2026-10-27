package com.fplassistant.dto;

public class SyncResponse {
    private String status;
    private String message;
    private Integer playersUpdated;
    private Integer fixturesUpdated;
    private Integer gameweeksUpdated;

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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SyncResponse instance = new SyncResponse();
        public Builder status(String v) { instance.setStatus(v); return this; }
        public Builder message(String v) { instance.setMessage(v); return this; }
        public Builder playersUpdated(Integer v) { instance.setPlayersUpdated(v); return this; }
        public Builder fixturesUpdated(Integer v) { instance.setFixturesUpdated(v); return this; }
        public Builder gameweeksUpdated(Integer v) { instance.setGameweeksUpdated(v); return this; }
        public SyncResponse build() { return instance; }
    }
}
