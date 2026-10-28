package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps an item in https://fantasy.premierleague.com/api/entry/{team_id}/transfers/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplTransferResponse {

    @JsonProperty("element_in")
    private Integer elementIn;

    @JsonProperty("element_in_cost")
    private Integer elementInCost;

    @JsonProperty("element_out")
    private Integer elementOut;

    @JsonProperty("element_out_cost")
    private Integer elementOutCost;

    private Integer entry;
    private Integer event;
    private String time;

    public Integer getElementIn() { return elementIn; }
    public void setElementIn(Integer elementIn) { this.elementIn = elementIn; }

    public Integer getElementInCost() { return elementInCost; }
    public void setElementInCost(Integer elementInCost) { this.elementInCost = elementInCost; }

    public Integer getElementOut() { return elementOut; }
    public void setElementOut(Integer elementOut) { this.elementOut = elementOut; }

    public Integer getElementOutCost() { return elementOutCost; }
    public void setElementOutCost(Integer elementOutCost) { this.elementOutCost = elementOutCost; }

    public Integer getEntry() { return entry; }
    public void setEntry(Integer entry) { this.entry = entry; }

    public Integer getEvent() { return event; }
    public void setEvent(Integer event) { this.event = event; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
