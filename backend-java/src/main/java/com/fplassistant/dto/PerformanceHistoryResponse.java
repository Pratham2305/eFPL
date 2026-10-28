package com.fplassistant.dto;

import java.util.List;

public class PerformanceHistoryResponse {

    private SeasonKpiDto summary;
    private List<GameweekHistoryDto> gameweeks;
    private List<String> deterministicInsights;
    private List<ChipUsageDto> chipsTimeline;

    public SeasonKpiDto getSummary() { return summary; }
    public void setSummary(SeasonKpiDto summary) { this.summary = summary; }

    public List<GameweekHistoryDto> getGameweeks() { return gameweeks; }
    public void setGameweeks(List<GameweekHistoryDto> gameweeks) { this.gameweeks = gameweeks; }

    public List<String> getDeterministicInsights() { return deterministicInsights; }
    public void setDeterministicInsights(List<String> deterministicInsights) { this.deterministicInsights = deterministicInsights; }

    public List<ChipUsageDto> getChipsTimeline() { return chipsTimeline; }
    public void setChipsTimeline(List<ChipUsageDto> chipsTimeline) { this.chipsTimeline = chipsTimeline; }

    public static class SeasonKpiDto {
        private Integer totalPoints;
        private Integer currentOverallRank;
        private Integer bestGameweekRank;
        private Integer bestGameweekRankEvent;
        private Double averageGameweekScore;
        private Double averageLeagueScore;
        private Double scoreVersusLeagueAvg;
        private Integer totalTransfers;
        private Integer totalTransferCost;
        private Integer totalBenchPoints;
        private Double teamValue;
        private Double bank;
        private CaptainEfficiencyDto captainEfficiency;

        public Integer getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

        public Integer getCurrentOverallRank() { return currentOverallRank; }
        public void setCurrentOverallRank(Integer currentOverallRank) { this.currentOverallRank = currentOverallRank; }

        public Integer getBestGameweekRank() { return bestGameweekRank; }
        public void setBestGameweekRank(Integer bestGameweekRank) { this.bestGameweekRank = bestGameweekRank; }

        public Integer getBestGameweekRankEvent() { return bestGameweekRankEvent; }
        public void setBestGameweekRankEvent(Integer bestGameweekRankEvent) { this.bestGameweekRankEvent = bestGameweekRankEvent; }

        public Double getAverageGameweekScore() { return averageGameweekScore; }
        public void setAverageGameweekScore(Double averageGameweekScore) { this.averageGameweekScore = averageGameweekScore; }

        public Double getAverageLeagueScore() { return averageLeagueScore; }
        public void setAverageLeagueScore(Double averageLeagueScore) { this.averageLeagueScore = averageLeagueScore; }

        public Double getScoreVersusLeagueAvg() { return scoreVersusLeagueAvg; }
        public void setScoreVersusLeagueAvg(Double scoreVersusLeagueAvg) { this.scoreVersusLeagueAvg = scoreVersusLeagueAvg; }

        public Integer getTotalTransfers() { return totalTransfers; }
        public void setTotalTransfers(Integer totalTransfers) { this.totalTransfers = totalTransfers; }

        public Integer getTotalTransferCost() { return totalTransferCost; }
        public void setTotalTransferCost(Integer totalTransferCost) { this.totalTransferCost = totalTransferCost; }

        public Integer getTotalBenchPoints() { return totalBenchPoints; }
        public void setTotalBenchPoints(Integer totalBenchPoints) { this.totalBenchPoints = totalBenchPoints; }

        public Double getTeamValue() { return teamValue; }
        public void setTeamValue(Double teamValue) { this.teamValue = teamValue; }

        public Double getBank() { return bank; }
        public void setBank(Double bank) { this.bank = bank; }

        public CaptainEfficiencyDto getCaptainEfficiency() { return captainEfficiency; }
        public void setCaptainEfficiency(CaptainEfficiencyDto captainEfficiency) { this.captainEfficiency = captainEfficiency; }
    }

    public static class CaptainEfficiencyDto {
        private Integer totalCaptainPoints;
        private Double averageCaptainPoints;
        private Integer optimalCaptainHits;
        private Integer totalGameweeks;

        public Integer getTotalCaptainPoints() { return totalCaptainPoints; }
        public void setTotalCaptainPoints(Integer totalCaptainPoints) { this.totalCaptainPoints = totalCaptainPoints; }

        public Double getAverageCaptainPoints() { return averageCaptainPoints; }
        public void setAverageCaptainPoints(Double averageCaptainPoints) { this.averageCaptainPoints = averageCaptainPoints; }

        public Integer getOptimalCaptainHits() { return optimalCaptainHits; }
        public void setOptimalCaptainHits(Integer optimalCaptainHits) { this.optimalCaptainHits = optimalCaptainHits; }

        public Integer getTotalGameweeks() { return totalGameweeks; }
        public void setTotalGameweeks(Integer totalGameweeks) { this.totalGameweeks = totalGameweeks; }
    }

    public static class GameweekHistoryDto {
        private Integer gameweekId;
        private String gameweekName;
        private Integer points;
        private Integer netPoints;
        private Integer averageScore;
        private Integer highestScore;
        private Integer rank;
        private Integer overallRank;
        private Integer overallPoints;
        private Integer transfers;
        private Integer transferCost;
        private Integer pointsOnBench;
        private Double bank;
        private Double teamValue;
        private String chipUsed;
        private String captainName;
        private Integer captainPoints;

        public Integer getGameweekId() { return gameweekId; }
        public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }

        public String getGameweekName() { return gameweekName; }
        public void setGameweekName(String gameweekName) { this.gameweekName = gameweekName; }

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }

        public Integer getNetPoints() { return netPoints; }
        public void setNetPoints(Integer netPoints) { this.netPoints = netPoints; }

        public Integer getAverageScore() { return averageScore; }
        public void setAverageScore(Integer averageScore) { this.averageScore = averageScore; }

        public Integer getHighestScore() { return highestScore; }
        public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }

        public Integer getOverallRank() { return overallRank; }
        public void setOverallRank(Integer overallRank) { this.overallRank = overallRank; }

        public Integer getOverallPoints() { return overallPoints; }
        public void setOverallPoints(Integer overallPoints) { this.overallPoints = overallPoints; }

        public Integer getTransfers() { return transfers; }
        public void setTransfers(Integer transfers) { this.transfers = transfers; }

        public Integer getTransferCost() { return transferCost; }
        public void setTransferCost(Integer transferCost) { this.transferCost = transferCost; }

        public Integer getPointsOnBench() { return pointsOnBench; }
        public void setPointsOnBench(Integer pointsOnBench) { this.pointsOnBench = pointsOnBench; }

        public Double getBank() { return bank; }
        public void setBank(Double bank) { this.bank = bank; }

        public Double getTeamValue() { return teamValue; }
        public void setTeamValue(Double teamValue) { this.teamValue = teamValue; }

        public String getChipUsed() { return chipUsed; }
        public void setChipUsed(String chipUsed) { this.chipUsed = chipUsed; }

        public String getCaptainName() { return captainName; }
        public void setCaptainName(String captainName) { this.captainName = captainName; }

        public Integer getCaptainPoints() { return captainPoints; }
        public void setCaptainPoints(Integer captainPoints) { this.captainPoints = captainPoints; }
    }

    public static class ChipUsageDto {
        private Integer gameweekId;
        private String chip;
        private String chipName;
        private Integer gameweekPoints;

        public Integer getGameweekId() { return gameweekId; }
        public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }

        public String getChip() { return chip; }
        public void setChip(String chip) { this.chip = chip; }

        public String getChipName() { return chipName; }
        public void setChipName(String chipName) { this.chipName = chipName; }

        public Integer getGameweekPoints() { return gameweekPoints; }
        public void setGameweekPoints(Integer gameweekPoints) { this.gameweekPoints = gameweekPoints; }
    }
}
