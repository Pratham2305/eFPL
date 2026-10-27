package com.fplassistant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlayerSummaryDto {
    private Integer id;
    private String webName;
    private String firstName;
    private String secondName;
    private Integer teamId;
    private String teamName;
    private String teamShortName;
    private Short elementType;
    private String positionName;
    private Integer nowCost;
    private Integer costChangeStart;
    private Integer costChangeEvent;
    private String status;
    private Integer chanceOfPlayingNext;
    private Integer chanceOfPlayingThisRound;
    private String news;
    private LocalDateTime newsAdded;
    private BigDecimal form;
    private Integer totalPoints;
    private BigDecimal pointsPerGame;
    private BigDecimal selectedByPercent;
    private Integer minutes;
    private Integer starts;
    private Integer goalsScored;
    private Integer assists;
    private Integer cleanSheets;
    private Integer goalsConceded;
    private Integer ownGoals;
    private Integer penaltiesSaved;
    private Integer penaltiesMissed;
    private Integer yellowCards;
    private Integer redCards;
    private Integer saves;
    private Integer bonus;
    private Integer bps;
    private BigDecimal influence;
    private BigDecimal creativity;
    private BigDecimal threat;
    private BigDecimal ictIndex;
    private BigDecimal expectedGoals;
    private BigDecimal expectedAssists;
    private BigDecimal expectedGoalInvolvements;
    private BigDecimal expectedGoalsConceded;
    private Integer transfersInEvent;
    private Integer transfersOutEvent;
    private BigDecimal epNext;
    private BigDecimal xpNext;
    private boolean inSquad;
    private boolean isStarting;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private String nextOpponentShortName;
    private Integer nextOpponentDifficulty;
    private boolean isDoubleGameweek;
    private boolean isBlankGameweek;

    public PlayerSummaryDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getWebName() { return webName; }
    public void setWebName(String webName) { this.webName = webName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamShortName() { return teamShortName; }
    public void setTeamShortName(String teamShortName) { this.teamShortName = teamShortName; }
    public Short getElementType() { return elementType; }
    public void setElementType(Short elementType) { this.elementType = elementType; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public Integer getNowCost() { return nowCost; }
    public void setNowCost(Integer nowCost) { this.nowCost = nowCost; }
    public Integer getCostChangeStart() { return costChangeStart; }
    public void setCostChangeStart(Integer costChangeStart) { this.costChangeStart = costChangeStart; }
    public Integer getCostChangeEvent() { return costChangeEvent; }
    public void setCostChangeEvent(Integer costChangeEvent) { this.costChangeEvent = costChangeEvent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getChanceOfPlayingNext() { return chanceOfPlayingNext; }
    public void setChanceOfPlayingNext(Integer chanceOfPlayingNext) { this.chanceOfPlayingNext = chanceOfPlayingNext; }
    public Integer getChanceOfPlayingThisRound() { return chanceOfPlayingThisRound; }
    public void setChanceOfPlayingThisRound(Integer chanceOfPlayingThisRound) { this.chanceOfPlayingThisRound = chanceOfPlayingThisRound; }
    public String getNews() { return news; }
    public void setNews(String news) { this.news = news; }
    public LocalDateTime getNewsAdded() { return newsAdded; }
    public void setNewsAdded(LocalDateTime newsAdded) { this.newsAdded = newsAdded; }
    public BigDecimal getForm() { return form; }
    public void setForm(BigDecimal form) { this.form = form; }
    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }
    public BigDecimal getPointsPerGame() { return pointsPerGame; }
    public void setPointsPerGame(BigDecimal pointsPerGame) { this.pointsPerGame = pointsPerGame; }
    public BigDecimal getSelectedByPercent() { return selectedByPercent; }
    public void setSelectedByPercent(BigDecimal selectedByPercent) { this.selectedByPercent = selectedByPercent; }
    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }
    public Integer getStarts() { return starts; }
    public void setStarts(Integer starts) { this.starts = starts; }
    public Integer getGoalsScored() { return goalsScored; }
    public void setGoalsScored(Integer goalsScored) { this.goalsScored = goalsScored; }
    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }
    public Integer getCleanSheets() { return cleanSheets; }
    public void setCleanSheets(Integer cleanSheets) { this.cleanSheets = cleanSheets; }
    public Integer getGoalsConceded() { return goalsConceded; }
    public void setGoalsConceded(Integer goalsConceded) { this.goalsConceded = goalsConceded; }
    public Integer getOwnGoals() { return ownGoals; }
    public void setOwnGoals(Integer ownGoals) { this.ownGoals = ownGoals; }
    public Integer getPenaltiesSaved() { return penaltiesSaved; }
    public void setPenaltiesSaved(Integer penaltiesSaved) { this.penaltiesSaved = penaltiesSaved; }
    public Integer getPenaltiesMissed() { return penaltiesMissed; }
    public void setPenaltiesMissed(Integer penaltiesMissed) { this.penaltiesMissed = penaltiesMissed; }
    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }
    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }
    public Integer getSaves() { return saves; }
    public void setSaves(Integer saves) { this.saves = saves; }
    public Integer getBonus() { return bonus; }
    public void setBonus(Integer bonus) { this.bonus = bonus; }
    public Integer getBps() { return bps; }
    public void setBps(Integer bps) { this.bps = bps; }
    public BigDecimal getInfluence() { return influence; }
    public void setInfluence(BigDecimal influence) { this.influence = influence; }
    public BigDecimal getCreativity() { return creativity; }
    public void setCreativity(BigDecimal creativity) { this.creativity = creativity; }
    public BigDecimal getThreat() { return threat; }
    public void setThreat(BigDecimal threat) { this.threat = threat; }
    public BigDecimal getIctIndex() { return ictIndex; }
    public void setIctIndex(BigDecimal ictIndex) { this.ictIndex = ictIndex; }
    public BigDecimal getExpectedGoals() { return expectedGoals; }
    public void setExpectedGoals(BigDecimal expectedGoals) { this.expectedGoals = expectedGoals; }
    public BigDecimal getExpectedAssists() { return expectedAssists; }
    public void setExpectedAssists(BigDecimal expectedAssists) { this.expectedAssists = expectedAssists; }
    public BigDecimal getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
    public void setExpectedGoalInvolvements(BigDecimal expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; }
    public BigDecimal getExpectedGoalsConceded() { return expectedGoalsConceded; }
    public void setExpectedGoalsConceded(BigDecimal expectedGoalsConceded) { this.expectedGoalsConceded = expectedGoalsConceded; }
    public Integer getTransfersInEvent() { return transfersInEvent; }
    public void setTransfersInEvent(Integer transfersInEvent) { this.transfersInEvent = transfersInEvent; }
    public Integer getTransfersOutEvent() { return transfersOutEvent; }
    public void setTransfersOutEvent(Integer transfersOutEvent) { this.transfersOutEvent = transfersOutEvent; }
    public BigDecimal getEpNext() { return epNext; }
    public void setEpNext(BigDecimal epNext) { this.epNext = epNext; }
    public BigDecimal getXpNext() { return xpNext; }
    public void setXpNext(BigDecimal xpNext) { this.xpNext = xpNext; }
    public boolean isInSquad() { return inSquad; }
    public void setInSquad(boolean inSquad) { this.inSquad = inSquad; }
    public boolean isStarting() { return isStarting; }
    public void setStarting(boolean starting) { isStarting = starting; }
    public boolean isCaptain() { return isCaptain; }
    public void setCaptain(boolean captain) { isCaptain = captain; }
    public boolean isViceCaptain() { return isViceCaptain; }
    public void setViceCaptain(boolean viceCaptain) { isViceCaptain = viceCaptain; }
    public String getNextOpponentShortName() { return nextOpponentShortName; }
    public void setNextOpponentShortName(String nextOpponentShortName) { this.nextOpponentShortName = nextOpponentShortName; }
    public Integer getNextOpponentDifficulty() { return nextOpponentDifficulty; }
    public void setNextOpponentDifficulty(Integer nextOpponentDifficulty) { this.nextOpponentDifficulty = nextOpponentDifficulty; }
    public boolean isDoubleGameweek() { return isDoubleGameweek; }
    public void setDoubleGameweek(boolean doubleGameweek) { isDoubleGameweek = doubleGameweek; }
    public boolean isBlankGameweek() { return isBlankGameweek; }
    public void setBlankGameweek(boolean blankGameweek) { isBlankGameweek = blankGameweek; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PlayerSummaryDto d = new PlayerSummaryDto();
        public Builder id(Integer v) { d.setId(v); return this; }
        public Builder webName(String v) { d.setWebName(v); return this; }
        public Builder firstName(String v) { d.setFirstName(v); return this; }
        public Builder secondName(String v) { d.setSecondName(v); return this; }
        public Builder teamId(Integer v) { d.setTeamId(v); return this; }
        public Builder teamName(String v) { d.setTeamName(v); return this; }
        public Builder teamShortName(String v) { d.setTeamShortName(v); return this; }
        public Builder elementType(Short v) { d.setElementType(v); return this; }
        public Builder positionName(String v) { d.setPositionName(v); return this; }
        public Builder nowCost(Integer v) { d.setNowCost(v); return this; }
        public Builder costChangeStart(Integer v) { d.setCostChangeStart(v); return this; }
        public Builder costChangeEvent(Integer v) { d.setCostChangeEvent(v); return this; }
        public Builder status(String v) { d.setStatus(v); return this; }
        public Builder chanceOfPlayingNext(Integer v) { d.setChanceOfPlayingNext(v); return this; }
        public Builder chanceOfPlayingThisRound(Integer v) { d.setChanceOfPlayingThisRound(v); return this; }
        public Builder news(String v) { d.setNews(v); return this; }
        public Builder newsAdded(LocalDateTime v) { d.setNewsAdded(v); return this; }
        public Builder form(BigDecimal v) { d.setForm(v); return this; }
        public Builder totalPoints(Integer v) { d.setTotalPoints(v); return this; }
        public Builder pointsPerGame(BigDecimal v) { d.setPointsPerGame(v); return this; }
        public Builder selectedByPercent(BigDecimal v) { d.setSelectedByPercent(v); return this; }
        public Builder minutes(Integer v) { d.setMinutes(v); return this; }
        public Builder starts(Integer v) { d.setStarts(v); return this; }
        public Builder goalsScored(Integer v) { d.setGoalsScored(v); return this; }
        public Builder assists(Integer v) { d.setAssists(v); return this; }
        public Builder cleanSheets(Integer v) { d.setCleanSheets(v); return this; }
        public Builder goalsConceded(Integer v) { d.setGoalsConceded(v); return this; }
        public Builder ownGoals(Integer v) { d.setOwnGoals(v); return this; }
        public Builder penaltiesSaved(Integer v) { d.setPenaltiesSaved(v); return this; }
        public Builder penaltiesMissed(Integer v) { d.setPenaltiesMissed(v); return this; }
        public Builder yellowCards(Integer v) { d.setYellowCards(v); return this; }
        public Builder redCards(Integer v) { d.setRedCards(v); return this; }
        public Builder saves(Integer v) { d.setSaves(v); return this; }
        public Builder bonus(Integer v) { d.setBonus(v); return this; }
        public Builder bps(Integer v) { d.setBps(v); return this; }
        public Builder influence(BigDecimal v) { d.setInfluence(v); return this; }
        public Builder creativity(BigDecimal v) { d.setCreativity(v); return this; }
        public Builder threat(BigDecimal v) { d.setThreat(v); return this; }
        public Builder ictIndex(BigDecimal v) { d.setIctIndex(v); return this; }
        public Builder expectedGoals(BigDecimal v) { d.setExpectedGoals(v); return this; }
        public Builder expectedAssists(BigDecimal v) { d.setExpectedAssists(v); return this; }
        public Builder expectedGoalInvolvements(BigDecimal v) { d.setExpectedGoalInvolvements(v); return this; }
        public Builder expectedGoalsConceded(BigDecimal v) { d.setExpectedGoalsConceded(v); return this; }
        public Builder transfersInEvent(Integer v) { d.setTransfersInEvent(v); return this; }
        public Builder transfersOutEvent(Integer v) { d.setTransfersOutEvent(v); return this; }
        public Builder epNext(BigDecimal v) { d.setEpNext(v); return this; }
        public Builder xpNext(BigDecimal v) { d.setXpNext(v); return this; }
        public Builder inSquad(boolean v) { d.setInSquad(v); return this; }
        public Builder isStarting(boolean v) { d.setStarting(v); return this; }
        public Builder isCaptain(boolean v) { d.setCaptain(v); return this; }
        public Builder isViceCaptain(boolean v) { d.setViceCaptain(v); return this; }
        public Builder nextOpponentShortName(String v) { d.setNextOpponentShortName(v); return this; }
        public Builder nextOpponentDifficulty(Integer v) { d.setNextOpponentDifficulty(v); return this; }
        public Builder isDoubleGameweek(boolean v) { d.setDoubleGameweek(v); return this; }
        public Builder isBlankGameweek(boolean v) { d.setBlankGameweek(v); return this; }
        public PlayerSummaryDto build() { return d; }
    }
}
