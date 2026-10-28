package com.fplassistant.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for GET /api/team/current and GET /api/team/gameweek/{gw}
 */
public class TeamCurrentResponse {

    private Integer gameweekId;
    private Integer currentGameweekId;
    private Integer nextGameweekId;
    private String phase;
    private Boolean isProjected;
    private Integer overallRank;
    private Integer overallPoints;
    private Integer gameweekPoints;
    private Integer gameweekNetPoints;
    private Integer transfersCost;
    private Integer pointsOnBench;
    private Integer liveTotalPoints;
    private Integer reconciledGameweekPoints;
    private Boolean pointsDiscrepancy;
    private Integer averageEntryScore;
    private Integer highestScore;
    private String activeChip;
    private Integer bank;
    private Integer teamValue;
    private Integer freeTransfers;
    private ChipsAvailable chips;
    private List<PickDto> startingXI;
    private List<PickDto> bench;

    public Integer getGameweekId() { return gameweekId; }
    public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }

    public Integer getCurrentGameweekId() { return currentGameweekId; }
    public void setCurrentGameweekId(Integer currentGameweekId) { this.currentGameweekId = currentGameweekId; }

    public Integer getNextGameweekId() { return nextGameweekId; }
    public void setNextGameweekId(Integer nextGameweekId) { this.nextGameweekId = nextGameweekId; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public Boolean getIsProjected() { return isProjected; }
    public void setIsProjected(Boolean isProjected) { this.isProjected = isProjected; }

    public Integer getOverallRank() { return overallRank; }
    public void setOverallRank(Integer overallRank) { this.overallRank = overallRank; }

    public Integer getOverallPoints() { return overallPoints; }
    public void setOverallPoints(Integer overallPoints) { this.overallPoints = overallPoints; }

    public Integer getGameweekPoints() { return gameweekPoints; }
    public void setGameweekPoints(Integer gameweekPoints) { this.gameweekPoints = gameweekPoints; }

    public Integer getGameweekNetPoints() { return gameweekNetPoints; }
    public void setGameweekNetPoints(Integer gameweekNetPoints) { this.gameweekNetPoints = gameweekNetPoints; }

    public Integer getTransfersCost() { return transfersCost; }
    public void setTransfersCost(Integer transfersCost) { this.transfersCost = transfersCost; }

    public Integer getPointsOnBench() { return pointsOnBench; }
    public void setPointsOnBench(Integer pointsOnBench) { this.pointsOnBench = pointsOnBench; }

    public Integer getLiveTotalPoints() { return liveTotalPoints; }
    public void setLiveTotalPoints(Integer liveTotalPoints) { this.liveTotalPoints = liveTotalPoints; }

    public Integer getReconciledGameweekPoints() { return reconciledGameweekPoints; }
    public void setReconciledGameweekPoints(Integer reconciledGameweekPoints) { this.reconciledGameweekPoints = reconciledGameweekPoints; }

    public Boolean getPointsDiscrepancy() { return pointsDiscrepancy; }
    public void setPointsDiscrepancy(Boolean pointsDiscrepancy) { this.pointsDiscrepancy = pointsDiscrepancy; }

    public Integer getAverageEntryScore() { return averageEntryScore; }
    public void setAverageEntryScore(Integer averageEntryScore) { this.averageEntryScore = averageEntryScore; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    public String getActiveChip() { return activeChip; }
    public void setActiveChip(String activeChip) { this.activeChip = activeChip; }

    public Integer getBank() { return bank; }
    public void setBank(Integer bank) { this.bank = bank; }

    public Integer getTeamValue() { return teamValue; }
    public void setTeamValue(Integer teamValue) { this.teamValue = teamValue; }

    public Integer getFreeTransfers() { return freeTransfers; }
    public void setFreeTransfers(Integer freeTransfers) { this.freeTransfers = freeTransfers; }

    public ChipsAvailable getChips() { return chips; }
    public void setChips(ChipsAvailable chips) { this.chips = chips; }

    public List<PickDto> getStartingXI() { return startingXI; }
    public void setStartingXI(List<PickDto> startingXI) { this.startingXI = startingXI; }

    public List<PickDto> getBench() { return bench; }
    public void setBench(List<PickDto> bench) { this.bench = bench; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TeamCurrentResponse instance = new TeamCurrentResponse();
        public Builder gameweekId(Integer v) { instance.setGameweekId(v); return this; }
        public Builder currentGameweekId(Integer v) { instance.setCurrentGameweekId(v); return this; }
        public Builder nextGameweekId(Integer v) { instance.setNextGameweekId(v); return this; }
        public Builder phase(String v) { instance.setPhase(v); return this; }
        public Builder isProjected(Boolean v) { instance.setIsProjected(v); return this; }
        public Builder overallRank(Integer v) { instance.setOverallRank(v); return this; }
        public Builder overallPoints(Integer v) { instance.setOverallPoints(v); return this; }
        public Builder gameweekPoints(Integer v) { instance.setGameweekPoints(v); return this; }
        public Builder gameweekNetPoints(Integer v) { instance.setGameweekNetPoints(v); return this; }
        public Builder transfersCost(Integer v) { instance.setTransfersCost(v); return this; }
        public Builder pointsOnBench(Integer v) { instance.setPointsOnBench(v); return this; }
        public Builder liveTotalPoints(Integer v) { instance.setLiveTotalPoints(v); return this; }
        public Builder reconciledGameweekPoints(Integer v) { instance.setReconciledGameweekPoints(v); return this; }
        public Builder pointsDiscrepancy(Boolean v) { instance.setPointsDiscrepancy(v); return this; }
        public Builder averageEntryScore(Integer v) { instance.setAverageEntryScore(v); return this; }
        public Builder highestScore(Integer v) { instance.setHighestScore(v); return this; }
        public Builder activeChip(String v) { instance.setActiveChip(v); return this; }
        public Builder bank(Integer v) { instance.setBank(v); return this; }
        public Builder teamValue(Integer v) { instance.setTeamValue(v); return this; }
        public Builder freeTransfers(Integer v) { instance.setFreeTransfers(v); return this; }
        public Builder chips(ChipsAvailable v) { instance.setChips(v); return this; }
        public Builder startingXI(List<PickDto> v) { instance.setStartingXI(v); return this; }
        public Builder bench(List<PickDto> v) { instance.setBench(v); return this; }
        public TeamCurrentResponse build() { return instance; }
    }

    public static class ChipsAvailable {
        private Boolean wildcard;
        private Boolean freeHit;
        private Boolean benchBoost;
        private Boolean tripleCaptain;

        public Boolean getWildcard() { return wildcard; }
        public void setWildcard(Boolean wildcard) { this.wildcard = wildcard; }

        public Boolean getFreeHit() { return freeHit; }
        public void setFreeHit(Boolean freeHit) { this.freeHit = freeHit; }

        public Boolean getBenchBoost() { return benchBoost; }
        public void setBenchBoost(Boolean benchBoost) { this.benchBoost = benchBoost; }

        public Boolean getTripleCaptain() { return tripleCaptain; }
        public void setTripleCaptain(Boolean tripleCaptain) { this.tripleCaptain = tripleCaptain; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ChipsAvailable instance = new ChipsAvailable();
            public Builder wildcard(Boolean v) { instance.setWildcard(v); return this; }
            public Builder freeHit(Boolean v) { instance.setFreeHit(v); return this; }
            public Builder benchBoost(Boolean v) { instance.setBenchBoost(v); return this; }
            public Builder tripleCaptain(Boolean v) { instance.setTripleCaptain(v); return this; }
            public ChipsAvailable build() { return instance; }
        }
    }

    public static class PickDto {
        private Integer playerId;
        private String webName;
        private String teamShortName;
        private Integer teamCode;
        private String badgeUrl;
        private String position;
        private Integer squadPosition;
        private Integer multiplier;
        private Boolean isCaptain;
        private Boolean isViceCaptain;
        private Integer nowCost;
        private BigDecimal form;
        private Integer totalPoints;
        private Integer eventPoints;
        private Integer rawPoints;
        private Integer effectivePoints;
        private Integer minutes;
        private Integer goals;
        private Integer assists;
        private Integer cleanSheets;
        private Integer bonus;
        private Integer bps;
        private Integer yellowCards;
        private Integer redCards;
        private Boolean isAutoSubIn;
        private Boolean isAutoSubOut;
        private String subbedWithPlayerName;
        private BigDecimal expectedPoints;
        private String status;
        private String news;

        public Integer getPlayerId() { return playerId; }
        public void setPlayerId(Integer playerId) { this.playerId = playerId; }

        public String getWebName() { return webName; }
        public void setWebName(String webName) { this.webName = webName; }

        public String getTeamShortName() { return teamShortName; }
        public void setTeamShortName(String teamShortName) { this.teamShortName = teamShortName; }

        public Integer getTeamCode() { return teamCode; }
        public void setTeamCode(Integer teamCode) { this.teamCode = teamCode; }

        public String getBadgeUrl() { return badgeUrl; }
        public void setBadgeUrl(String badgeUrl) { this.badgeUrl = badgeUrl; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public Integer getSquadPosition() { return squadPosition; }
        public void setSquadPosition(Integer squadPosition) { this.squadPosition = squadPosition; }

        public Integer getMultiplier() { return multiplier; }
        public void setMultiplier(Integer multiplier) { this.multiplier = multiplier; }

        public Boolean getIsCaptain() { return isCaptain; }
        public void setIsCaptain(Boolean isCaptain) { this.isCaptain = isCaptain; }

        public Boolean getIsViceCaptain() { return isViceCaptain; }
        public void setIsViceCaptain(Boolean isViceCaptain) { this.isViceCaptain = isViceCaptain; }

        public Integer getNowCost() { return nowCost; }
        public void setNowCost(Integer nowCost) { this.nowCost = nowCost; }

        public BigDecimal getForm() { return form; }
        public void setForm(BigDecimal form) { this.form = form; }

        public Integer getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

        public Integer getEventPoints() { return eventPoints; }
        public void setEventPoints(Integer eventPoints) { this.eventPoints = eventPoints; }

        public Integer getRawPoints() { return rawPoints; }
        public void setRawPoints(Integer rawPoints) { this.rawPoints = rawPoints; }

        public Integer getEffectivePoints() { return effectivePoints; }
        public void setEffectivePoints(Integer effectivePoints) { this.effectivePoints = effectivePoints; }

        public Integer getMinutes() { return minutes; }
        public void setMinutes(Integer minutes) { this.minutes = minutes; }

        public Integer getGoals() { return goals; }
        public void setGoals(Integer goals) { this.goals = goals; }

        public Integer getAssists() { return assists; }
        public void setAssists(Integer assists) { this.assists = assists; }

        public Integer getCleanSheets() { return cleanSheets; }
        public void setCleanSheets(Integer cleanSheets) { this.cleanSheets = cleanSheets; }

        public Integer getBonus() { return bonus; }
        public void setBonus(Integer bonus) { this.bonus = bonus; }

        public Integer getBps() { return bps; }
        public void setBps(Integer bps) { this.bps = bps; }

        public Integer getYellowCards() { return yellowCards; }
        public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }

        public Integer getRedCards() { return redCards; }
        public void setRedCards(Integer redCards) { this.redCards = redCards; }

        public Boolean getIsAutoSubIn() { return isAutoSubIn; }
        public void setIsAutoSubIn(Boolean isAutoSubIn) { this.isAutoSubIn = isAutoSubIn; }

        public Boolean getIsAutoSubOut() { return isAutoSubOut; }
        public void setIsAutoSubOut(Boolean isAutoSubOut) { this.isAutoSubOut = isAutoSubOut; }

        public String getSubbedWithPlayerName() { return subbedWithPlayerName; }
        public void setSubbedWithPlayerName(String subbedWithPlayerName) { this.subbedWithPlayerName = subbedWithPlayerName; }

        public BigDecimal getExpectedPoints() { return expectedPoints; }
        public void setExpectedPoints(BigDecimal expectedPoints) { this.expectedPoints = expectedPoints; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNews() { return news; }
        public void setNews(String news) { this.news = news; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final PickDto instance = new PickDto();
            public Builder playerId(Integer v) { instance.setPlayerId(v); return this; }
            public Builder webName(String v) { instance.setWebName(v); return this; }
            public Builder teamShortName(String v) { instance.setTeamShortName(v); return this; }
            public Builder teamCode(Integer v) { instance.setTeamCode(v); return this; }
            public Builder badgeUrl(String v) { instance.setBadgeUrl(v); return this; }
            public Builder position(String v) { instance.setPosition(v); return this; }
            public Builder squadPosition(Integer v) { instance.setSquadPosition(v); return this; }
            public Builder multiplier(Integer v) { instance.setMultiplier(v); return this; }
            public Builder isCaptain(Boolean v) { instance.setIsCaptain(v); return this; }
            public Builder isViceCaptain(Boolean v) { instance.setIsViceCaptain(v); return this; }
            public Builder nowCost(Integer v) { instance.setNowCost(v); return this; }
            public Builder form(BigDecimal v) { instance.setForm(v); return this; }
            public Builder totalPoints(Integer v) { instance.setTotalPoints(v); return this; }
            public Builder eventPoints(Integer v) { instance.setEventPoints(v); return this; }
            public Builder rawPoints(Integer v) { instance.setRawPoints(v); return this; }
            public Builder effectivePoints(Integer v) { instance.setEffectivePoints(v); return this; }
            public Builder minutes(Integer v) { instance.setMinutes(v); return this; }
            public Builder goals(Integer v) { instance.setGoals(v); return this; }
            public Builder assists(Integer v) { instance.setAssists(v); return this; }
            public Builder cleanSheets(Integer v) { instance.setCleanSheets(v); return this; }
            public Builder bonus(Integer v) { instance.setBonus(v); return this; }
            public Builder bps(Integer v) { instance.setBps(v); return this; }
            public Builder yellowCards(Integer v) { instance.setYellowCards(v); return this; }
            public Builder redCards(Integer v) { instance.setRedCards(v); return this; }
            public Builder isAutoSubIn(Boolean v) { instance.setIsAutoSubIn(v); return this; }
            public Builder isAutoSubOut(Boolean v) { instance.setIsAutoSubOut(v); return this; }
            public Builder subbedWithPlayerName(String v) { instance.setSubbedWithPlayerName(v); return this; }
            public Builder expectedPoints(BigDecimal v) { instance.setExpectedPoints(v); return this; }
            public Builder status(String v) { instance.setStatus(v); return this; }
            public Builder news(String v) { instance.setNews(v); return this; }
            public PickDto build() { return instance; }
        }
    }
}
