package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the JSON from https://fantasy.premierleague.com/api/bootstrap-static/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplBootstrapResponse {

    private List<FplEvent> events;
    private List<FplElement> elements;
    private List<FplTeam> teams;

    public List<FplEvent> getEvents() { return events; }
    public void setEvents(List<FplEvent> events) { this.events = events; }

    public List<FplElement> getElements() { return elements; }
    public void setElements(List<FplElement> elements) { this.elements = elements; }

    public List<FplTeam> getTeams() { return teams; }
    public void setTeams(List<FplTeam> teams) { this.teams = teams; }

    // ─── events (gameweeks) ─────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplEvent {
        private Integer id;
        private String name;

        @JsonProperty("deadline_time")
        private String deadlineTime;

        @JsonProperty("average_entry_score")
        private Integer averageEntryScore;

        @JsonProperty("highest_score")
        private Integer highestScore;

        @JsonProperty("is_current")
        private Boolean isCurrent;

        @JsonProperty("is_next")
        private Boolean isNext;

        @JsonProperty("is_previous")
        private Boolean isPrevious;

        private Boolean finished;

        @JsonProperty("data_checked")
        private Boolean dataChecked;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDeadlineTime() { return deadlineTime; }
        public void setDeadlineTime(String deadlineTime) { this.deadlineTime = deadlineTime; }

        public Integer getAverageEntryScore() { return averageEntryScore; }
        public void setAverageEntryScore(Integer averageEntryScore) { this.averageEntryScore = averageEntryScore; }

        public Integer getHighestScore() { return highestScore; }
        public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

        public Boolean getIsCurrent() { return isCurrent; }
        public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }

        public Boolean getIsNext() { return isNext; }
        public void setIsNext(Boolean isNext) { this.isNext = isNext; }

        public Boolean getIsPrevious() { return isPrevious; }
        public void setIsPrevious(Boolean isPrevious) { this.isPrevious = isPrevious; }

        public Boolean getFinished() { return finished; }
        public void setFinished(Boolean finished) { this.finished = finished; }

        public Boolean getDataChecked() { return dataChecked; }
        public void setDataChecked(Boolean dataChecked) { this.dataChecked = dataChecked; }
    }

    // ─── elements (players) ─────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplElement {
        private Integer id;
        private Integer team;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("second_name")
        private String secondName;

        @JsonProperty("web_name")
        private String webName;

        @JsonProperty("element_type")
        private Integer elementType;

        @JsonProperty("now_cost")
        private Integer nowCost;

        private String status;

        @JsonProperty("chance_of_playing_next_round")
        private Integer chanceOfPlayingNextRound;

        @JsonProperty("chance_of_playing_this_round")
        private Integer chanceOfPlayingThisRound;

        private String news;

        @JsonProperty("news_added")
        private String newsAdded;

        private String photo;
        private String form;

        @JsonProperty("total_points")
        private Integer totalPoints;

        @JsonProperty("points_per_game")
        private String pointsPerGame;

        @JsonProperty("selected_by_percent")
        private String selectedByPercent;

        private Integer minutes;

        @JsonProperty("starts")
        private Integer starts;

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
        private String influence;
        private String creativity;
        private String threat;

        @JsonProperty("ict_index")
        private String ictIndex;

        @JsonProperty("expected_goals")
        private String expectedGoals;

        @JsonProperty("expected_assists")
        private String expectedAssists;

        @JsonProperty("expected_goal_involvements")
        private String expectedGoalInvolvements;

        @JsonProperty("expected_goals_conceded")
        private String expectedGoalsConceded;

        @JsonProperty("transfers_in_event")
        private Integer transfersInEvent;

        @JsonProperty("transfers_out_event")
        private Integer transfersOutEvent;

        @JsonProperty("cost_change_start")
        private Integer costChangeStart;

        @JsonProperty("cost_change_event")
        private Integer costChangeEvent;

        @JsonProperty("ep_next")
        private String epNext;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getTeam() { return team; }
        public void setTeam(Integer team) { this.team = team; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getSecondName() { return secondName; }
        public void setSecondName(String secondName) { this.secondName = secondName; }

        public String getWebName() { return webName; }
        public void setWebName(String webName) { this.webName = webName; }

        public Integer getElementType() { return elementType; }
        public void setElementType(Integer elementType) { this.elementType = elementType; }

        public Integer getNowCost() { return nowCost; }
        public void setNowCost(Integer nowCost) { this.nowCost = nowCost; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getChanceOfPlayingNextRound() { return chanceOfPlayingNextRound; }
        public void setChanceOfPlayingNextRound(Integer chanceOfPlayingNextRound) { this.chanceOfPlayingNextRound = chanceOfPlayingNextRound; }

        public Integer getChanceOfPlayingThisRound() { return chanceOfPlayingThisRound; }
        public void setChanceOfPlayingThisRound(Integer chanceOfPlayingThisRound) { this.chanceOfPlayingThisRound = chanceOfPlayingThisRound; }

        public String getNews() { return news; }
        public void setNews(String news) { this.news = news; }

        public String getNewsAdded() { return newsAdded; }
        public void setNewsAdded(String newsAdded) { this.newsAdded = newsAdded; }

        public String getPhoto() { return photo; }
        public void setPhoto(String photo) { this.photo = photo; }

        public String getForm() { return form; }
        public void setForm(String form) { this.form = form; }

        public Integer getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

        public String getPointsPerGame() { return pointsPerGame; }
        public void setPointsPerGame(String pointsPerGame) { this.pointsPerGame = pointsPerGame; }

        public String getSelectedByPercent() { return selectedByPercent; }
        public void setSelectedByPercent(String selectedByPercent) { this.selectedByPercent = selectedByPercent; }

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

        public String getInfluence() { return influence; }
        public void setInfluence(String influence) { this.influence = influence; }

        public String getCreativity() { return creativity; }
        public void setCreativity(String creativity) { this.creativity = creativity; }

        public String getThreat() { return threat; }
        public void setThreat(String threat) { this.threat = threat; }

        public String getIctIndex() { return ictIndex; }
        public void setIctIndex(String ictIndex) { this.ictIndex = ictIndex; }

        public String getExpectedGoals() { return expectedGoals; }
        public void setExpectedGoals(String expectedGoals) { this.expectedGoals = expectedGoals; }

        public String getExpectedAssists() { return expectedAssists; }
        public void setExpectedAssists(String expectedAssists) { this.expectedAssists = expectedAssists; }

        public String getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
        public void setExpectedGoalInvolvements(String expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; }

        public String getExpectedGoalsConceded() { return expectedGoalsConceded; }
        public void setExpectedGoalsConceded(String expectedGoalsConceded) { this.expectedGoalsConceded = expectedGoalsConceded; }

        public Integer getTransfersInEvent() { return transfersInEvent; }
        public void setTransfersInEvent(Integer transfersInEvent) { this.transfersInEvent = transfersInEvent; }

        public Integer getTransfersOutEvent() { return transfersOutEvent; }
        public void setTransfersOutEvent(Integer transfersOutEvent) { this.transfersOutEvent = transfersOutEvent; }

        public Integer getCostChangeStart() { return costChangeStart; }
        public void setCostChangeStart(Integer costChangeStart) { this.costChangeStart = costChangeStart; }

        public Integer getCostChangeEvent() { return costChangeEvent; }
        public void setCostChangeEvent(Integer costChangeEvent) { this.costChangeEvent = costChangeEvent; }

        public String getEpNext() { return epNext; }
        public void setEpNext(String epNext) { this.epNext = epNext; }
    }

    // ─── teams ──────────────────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplTeam {
        private Integer id;
        private String name;

        @JsonProperty("short_name")
        private String shortName;

        private Integer code;
        private Integer strength;

        @JsonProperty("strength_overall_home")
        private Integer strengthOverallHome;

        @JsonProperty("strength_overall_away")
        private Integer strengthOverallAway;

        @JsonProperty("strength_attack_home")
        private Integer strengthAttackHome;

        @JsonProperty("strength_attack_away")
        private Integer strengthAttackAway;

        @JsonProperty("strength_defence_home")
        private Integer strengthDefenceHome;

        @JsonProperty("strength_defence_away")
        private Integer strengthDefenceAway;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }

        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }

        public Integer getStrength() { return strength; }
        public void setStrength(Integer strength) { this.strength = strength; }

        public Integer getStrengthOverallHome() { return strengthOverallHome; }
        public void setStrengthOverallHome(Integer strengthOverallHome) { this.strengthOverallHome = strengthOverallHome; }

        public Integer getStrengthOverallAway() { return strengthOverallAway; }
        public void setStrengthOverallAway(Integer strengthOverallAway) { this.strengthOverallAway = strengthOverallAway; }

        public Integer getStrengthAttackHome() { return strengthAttackHome; }
        public void setStrengthAttackHome(Integer strengthAttackHome) { this.strengthAttackHome = strengthAttackHome; }

        public Integer getStrengthAttackAway() { return strengthAttackAway; }
        public void setStrengthAttackAway(Integer strengthAttackAway) { this.strengthAttackAway = strengthAttackAway; }

        public Integer getStrengthDefenceHome() { return strengthDefenceHome; }
        public void setStrengthDefenceHome(Integer strengthDefenceHome) { this.strengthDefenceHome = strengthDefenceHome; }

        public Integer getStrengthDefenceAway() { return strengthDefenceAway; }
        public void setStrengthDefenceAway(Integer strengthDefenceAway) { this.strengthDefenceAway = strengthDefenceAway; }
    }
}
