package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps https://fantasy.premierleague.com/api/entry/{team_id}/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplEntryResponse {

    private Integer id;

    @JsonProperty("player_first_name")
    private String playerFirstName;

    @JsonProperty("player_last_name")
    private String playerLastName;

    private String name;

    @JsonProperty("summary_overall_points")
    private Integer summaryOverallPoints;

    @JsonProperty("summary_overall_rank")
    private Integer summaryOverallRank;

    @JsonProperty("summary_event_points")
    private Integer summaryEventPoints;

    @JsonProperty("current_event")
    private Integer currentEvent;

    @JsonProperty("last_deadline_bank")
    private Integer lastDeadlineBank;

    @JsonProperty("last_deadline_value")
    private Integer lastDeadlineValue;

    @JsonProperty("last_deadline_total_transfers")
    private Integer lastDeadlineTotalTransfers;

    private Object leagues; // FPL API returns a JSON object for leagues

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPlayerFirstName() { return playerFirstName; }
    public void setPlayerFirstName(String playerFirstName) { this.playerFirstName = playerFirstName; }

    public String getPlayerLastName() { return playerLastName; }
    public void setPlayerLastName(String playerLastName) { this.playerLastName = playerLastName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSummaryOverallPoints() { return summaryOverallPoints; }
    public void setSummaryOverallPoints(Integer summaryOverallPoints) { this.summaryOverallPoints = summaryOverallPoints; }

    public Integer getSummaryOverallRank() { return summaryOverallRank; }
    public void setSummaryOverallRank(Integer summaryOverallRank) { this.summaryOverallRank = summaryOverallRank; }

    public Integer getSummaryEventPoints() { return summaryEventPoints; }
    public void setSummaryEventPoints(Integer summaryEventPoints) { this.summaryEventPoints = summaryEventPoints; }

    public Integer getCurrentEvent() { return currentEvent; }
    public void setCurrentEvent(Integer currentEvent) { this.currentEvent = currentEvent; }

    public Integer getLastDeadlineBank() { return lastDeadlineBank; }
    public void setLastDeadlineBank(Integer lastDeadlineBank) { this.lastDeadlineBank = lastDeadlineBank; }

    public Integer getLastDeadlineValue() { return lastDeadlineValue; }
    public void setLastDeadlineValue(Integer lastDeadlineValue) { this.lastDeadlineValue = lastDeadlineValue; }

    public Integer getLastDeadlineTotalTransfers() { return lastDeadlineTotalTransfers; }
    public void setLastDeadlineTotalTransfers(Integer lastDeadlineTotalTransfers) { this.lastDeadlineTotalTransfers = lastDeadlineTotalTransfers; }

    public Object getLeagues() { return leagues; }
    public void setLeagues(Object leagues) { this.leagues = leagues; }
}
