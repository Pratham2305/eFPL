package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps https://fantasy.premierleague.com/api/entry/{team_id}/history/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplHistoryResponse {

    private List<HistoryCurrent> current;
    private List<HistoryChip> chips;

    public List<HistoryCurrent> getCurrent() { return current; }
    public void setCurrent(List<HistoryCurrent> current) { this.current = current; }

    public List<HistoryChip> getChips() { return chips; }
    public void setChips(List<HistoryChip> chips) { this.chips = chips; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryCurrent {
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
    public static class HistoryChip {
        private String name;
        private String time;
        private Integer event;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public Integer getEvent() { return event; }
        public void setEvent(Integer event) { this.event = event; }
    }
}
