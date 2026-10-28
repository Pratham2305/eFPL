package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps https://fantasy.premierleague.com/api/event/{gw}/live/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplLiveResponse {

    private List<LiveElement> elements;

    public List<LiveElement> getElements() { return elements; }
    public void setElements(List<LiveElement> elements) { this.elements = elements; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LiveElement {
        private Integer id;
        private LiveStats stats;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public LiveStats getStats() { return stats; }
        public void setStats(LiveStats stats) { this.stats = stats; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LiveStats {
        private Integer minutes;

        @JsonProperty("goals_scored")
        private Integer goalsScored;

        private Integer assists;

        @JsonProperty("clean_sheets")
        private Integer cleanSheets;

        @JsonProperty("goals_conceded")
        private Integer goalsConceded;

        @JsonProperty("own_goals")
        private Integer ownGoals;

        @JsonProperty("penalties_saved")
        private Integer penaltiesSaved;

        @JsonProperty("penalties_missed")
        private Integer penaltiesMissed;

        @JsonProperty("yellow_cards")
        private Integer yellowCards;

        @JsonProperty("red_cards")
        private Integer redCards;

        private Integer saves;
        private Integer bonus;
        private Integer bps;
        private BigDecimal influence;
        private BigDecimal creativity;
        private BigDecimal threat;

        @JsonProperty("ict_index")
        private BigDecimal ictIndex;

        private Integer starts;

        @JsonProperty("expected_goals")
        private BigDecimal expectedGoals;

        @JsonProperty("expected_assists")
        private BigDecimal expectedAssists;

        @JsonProperty("expected_goal_involvements")
        private BigDecimal expectedGoalInvolvements;

        @JsonProperty("expected_goals_conceded")
        private BigDecimal expectedGoalsConceded;

        @JsonProperty("total_points")
        private Integer totalPoints;

        @JsonProperty("in_dreamteam")
        private Boolean inDreamteam;

        @JsonProperty("defensive_contribution")
        private Integer defensiveContribution;

        private Integer recoveries;
        private Integer tackles;

        @JsonProperty("clearances_blocks_interceptions")
        private Integer clearancesBlocksInterceptions;

        public Integer getMinutes() { return minutes; }
        public void setMinutes(Integer minutes) { this.minutes = minutes; }

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

        public Integer getStarts() { return starts; }
        public void setStarts(Integer starts) { this.starts = starts; }

        public BigDecimal getExpectedGoals() { return expectedGoals; }
        public void setExpectedGoals(BigDecimal expectedGoals) { this.expectedGoals = expectedGoals; }

        public BigDecimal getExpectedAssists() { return expectedAssists; }
        public void setExpectedAssists(BigDecimal expectedAssists) { this.expectedAssists = expectedAssists; }

        public BigDecimal getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
        public void setExpectedGoalInvolvements(BigDecimal expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; }

        public BigDecimal getExpectedGoalsConceded() { return expectedGoalsConceded; }
        public void setExpectedGoalsConceded(BigDecimal expectedGoalsConceded) { this.expectedGoalsConceded = expectedGoalsConceded; }

        public Integer getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

        public Boolean getInDreamteam() { return inDreamteam; }
        public void setInDreamteam(Boolean inDreamteam) { this.inDreamteam = inDreamteam; }

        public Integer getDefensiveContribution() { return defensiveContribution; }
        public void setDefensiveContribution(Integer defensiveContribution) { this.defensiveContribution = defensiveContribution; }

        public Integer getRecoveries() { return recoveries; }
        public void setRecoveries(Integer recoveries) { this.recoveries = recoveries; }

        public Integer getTackles() { return tackles; }
        public void setTackles(Integer tackles) { this.tackles = tackles; }

        public Integer getClearancesBlocksInterceptions() { return clearancesBlocksInterceptions; }
        public void setClearancesBlocksInterceptions(Integer clearancesBlocksInterceptions) { this.clearancesBlocksInterceptions = clearancesBlocksInterceptions; }
    }
}
