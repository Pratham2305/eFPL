package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps https://fantasy.premierleague.com/api/entry/{team_id}/event/{gw}/picks/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplPicksResponse {

    @JsonProperty("active_chip")
    private String activeChip;

    @JsonProperty("automatic_subs")
    private List<FplAutoSub> automaticSubs;

    @JsonProperty("entry_history")
    private FplEntryHistory entryHistory;

    private List<FplPick> picks;

    public String getActiveChip() { return activeChip; }
    public void setActiveChip(String activeChip) { this.activeChip = activeChip; }

    public List<FplAutoSub> getAutomaticSubs() { return automaticSubs; }
    public void setAutomaticSubs(List<FplAutoSub> automaticSubs) { this.automaticSubs = automaticSubs; }

    public FplEntryHistory getEntryHistory() { return entryHistory; }
    public void setEntryHistory(FplEntryHistory entryHistory) { this.entryHistory = entryHistory; }

    public List<FplPick> getPicks() { return picks; }
    public void setPicks(List<FplPick> picks) { this.picks = picks; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplAutoSub {
        private Integer entry;

        @JsonProperty("element_in")
        private Integer elementIn;

        @JsonProperty("element_out")
        private Integer elementOut;

        private Integer event;

        public Integer getEntry() { return entry; }
        public void setEntry(Integer entry) { this.entry = entry; }

        public Integer getElementIn() { return elementIn; }
        public void setElementIn(Integer elementIn) { this.elementIn = elementIn; }

        public Integer getElementOut() { return elementOut; }
        public void setElementOut(Integer elementOut) { this.elementOut = elementOut; }

        public Integer getEvent() { return event; }
        public void setEvent(Integer event) { this.event = event; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplEntryHistory {
        private Integer event;
        private Integer points;

        @JsonProperty("total_points")
        private Integer totalPoints;

        private Integer rank;

        @JsonProperty("rank_sort")
        private Integer rankSort;

        @JsonProperty("overall_rank")
        private Integer overallRank;

        private Integer bank;
        private Integer value;

        @JsonProperty("total_transfers")
        private Integer totalTransfers;

        @JsonProperty("event_transfers")
        private Integer eventTransfers;

        @JsonProperty("event_transfers_cost")
        private Integer eventTransfersCost;

        @JsonProperty("points_on_bench")
        private Integer pointsOnBench;

        public Integer getEvent() { return event; }
        public void setEvent(Integer event) { this.event = event; }

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }

        public Integer getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }

        public Integer getRankSort() { return rankSort; }
        public void setRankSort(Integer rankSort) { this.rankSort = rankSort; }

        public Integer getOverallRank() { return overallRank; }
        public void setOverallRank(Integer overallRank) { this.overallRank = overallRank; }

        public Integer getBank() { return bank; }
        public void setBank(Integer bank) { this.bank = bank; }

        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }

        public Integer getTotalTransfers() { return totalTransfers; }
        public void setTotalTransfers(Integer totalTransfers) { this.totalTransfers = totalTransfers; }

        public Integer getEventTransfers() { return eventTransfers; }
        public void setEventTransfers(Integer eventTransfers) { this.eventTransfers = eventTransfers; }

        public Integer getEventTransfersCost() { return eventTransfersCost; }
        public void setEventTransfersCost(Integer eventTransfersCost) { this.eventTransfersCost = eventTransfersCost; }

        public Integer getPointsOnBench() { return pointsOnBench; }
        public void setPointsOnBench(Integer pointsOnBench) { this.pointsOnBench = pointsOnBench; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplPick {
        private Integer element;
        private Integer position;
        private Integer multiplier;

        @JsonProperty("is_captain")
        private Boolean isCaptain;

        @JsonProperty("is_vice_captain")
        private Boolean isViceCaptain;

        public Integer getElement() { return element; }
        public void setElement(Integer element) { this.element = element; }

        public Integer getPosition() { return position; }
        public void setPosition(Integer position) { this.position = position; }

        public Integer getMultiplier() { return multiplier; }
        public void setMultiplier(Integer multiplier) { this.multiplier = multiplier; }

        public Boolean getIsCaptain() { return isCaptain; }
        public void setIsCaptain(Boolean isCaptain) { this.isCaptain = isCaptain; }

        public Boolean getIsViceCaptain() { return isViceCaptain; }
        public void setIsViceCaptain(Boolean isViceCaptain) { this.isViceCaptain = isViceCaptain; }
    }
}
