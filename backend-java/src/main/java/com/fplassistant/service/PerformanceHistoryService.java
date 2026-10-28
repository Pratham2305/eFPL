package com.fplassistant.service;

import com.fplassistant.dto.PerformanceHistoryResponse;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PerformanceHistoryService {

    private final MyTeamStateRepository myTeamStateRepo;
    private final MyTeamPickRepository myTeamPickRepo;
    private final PlayerGameweekStatsRepository statsRepo;
    private final GameweekRepository gameweekRepo;

    public PerformanceHistoryService(MyTeamStateRepository myTeamStateRepo,
                                     MyTeamPickRepository myTeamPickRepo,
                                     PlayerGameweekStatsRepository statsRepo,
                                     GameweekRepository gameweekRepo) {
        this.myTeamStateRepo = myTeamStateRepo;
        this.myTeamPickRepo = myTeamPickRepo;
        this.statsRepo = statsRepo;
        this.gameweekRepo = gameweekRepo;
    }

    @Transactional(readOnly = true)
    public PerformanceHistoryResponse getPerformanceHistory() {
        List<MyTeamState> states = myTeamStateRepo.findAll(Sort.by(Sort.Direction.ASC, "gameweek.id"));
        List<PerformanceHistoryResponse.GameweekHistoryDto> gwList = new ArrayList<>();
        List<PerformanceHistoryResponse.ChipUsageDto> chipsList = new ArrayList<>();

        int totalCaptainPoints = 0;
        int optimalCaptainHits = 0;
        int totalTransfers = 0;
        int totalTransferCost = 0;
        int totalBenchPoints = 0;

        int bestRank = Integer.MAX_VALUE;
        int bestRankGw = 1;
        int maxBenchPoints = -1;
        int maxBenchGw = 1;

        double totalNetPoints = 0;
        double totalLeagueAvgPoints = 0;
        int validGwCount = 0;

        for (MyTeamState st : states) {
            Gameweek gw = st.getGameweek();
            if (gw == null || st.getGameweekPoints() == null) continue;

            int gwId = gw.getId();
            int grossPoints = st.getGameweekPoints();
            int cost = st.getEventTransfersCost() != null ? st.getEventTransfersCost() : 0;
            int netPoints = st.getGameweekNetPoints() != null ? st.getGameweekNetPoints() : (grossPoints - cost);
            int benchPts = st.getPointsOnBench() != null ? st.getPointsOnBench() : 0;
            int transfers = st.getEventTransfers() != null ? st.getEventTransfers() : 0;

            totalTransfers += transfers;
            totalTransferCost += cost;
            totalBenchPoints += benchPts;
            totalNetPoints += netPoints;
            if (gw.getAverageEntryScore() != null) {
                totalLeagueAvgPoints += gw.getAverageEntryScore();
            }
            validGwCount++;

            if (st.getRank() != null && st.getRank() > 0 && st.getRank() < bestRank) {
                bestRank = st.getRank();
                bestRankGw = gwId;
            }

            if (benchPts > maxBenchPoints) {
                maxBenchPoints = benchPts;
                maxBenchGw = gwId;
            }

            // Determine captain and optimal captain
            List<MyTeamPick> picks = myTeamPickRepo.findByGameweekIdOrderByPositionAsc(gwId);
            String captainName = "Unknown";
            int captainEffectivePts = 0;
            int maxPlayerScore = -1;
            int captainRawScore = 0;

            for (MyTeamPick p : picks) {
                Player pl = p.getPlayer();
                PlayerGameweekStats pgs = statsRepo.findFirstByPlayerIdAndGameweekId(pl.getId(), gwId).orElse(null);
                int rawPts = (pgs != null && pgs.getTotalPoints() != null) ? pgs.getTotalPoints() : 0;
                if (rawPts > maxPlayerScore) {
                    maxPlayerScore = rawPts;
                }
            }

            // Identify effective captain pick (multiplier > 1 preferred in case of VC promotion)
            MyTeamPick activeCaptainPick = picks.stream()
                    .filter(p -> p.getMultiplier() != null && p.getMultiplier() > 1)
                    .findFirst()
                    .orElseGet(() -> picks.stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsCaptain()))
                            .findFirst()
                            .orElse(null));

            if (activeCaptainPick != null) {
                Player pl = activeCaptainPick.getPlayer();
                captainName = pl.getWebName();
                PlayerGameweekStats pgs = statsRepo.findFirstByPlayerIdAndGameweekId(pl.getId(), gwId).orElse(null);
                captainRawScore = (pgs != null && pgs.getTotalPoints() != null) ? pgs.getTotalPoints() : 0;
                int mult = activeCaptainPick.getMultiplier() != null ? activeCaptainPick.getMultiplier().intValue() : 1;
                captainEffectivePts = captainRawScore * mult;
            }

            if (captainRawScore >= maxPlayerScore && maxPlayerScore >= 0) {
                optimalCaptainHits++;
            }
            totalCaptainPoints += captainEffectivePts;

            // Chip tracking
            String chipUsed = st.getActiveChip();
            if (chipUsed != null && !chipUsed.isBlank()) {
                PerformanceHistoryResponse.ChipUsageDto chipDto = new PerformanceHistoryResponse.ChipUsageDto();
                chipDto.setGameweekId(gwId);
                chipDto.setChip(chipUsed);
                chipDto.setChipName(mapChipName(chipUsed));
                chipDto.setGameweekPoints(grossPoints);
                chipsList.add(chipDto);
            }

            PerformanceHistoryResponse.GameweekHistoryDto dto = new PerformanceHistoryResponse.GameweekHistoryDto();
            dto.setGameweekId(gwId);
            dto.setGameweekName("GW " + gwId);
            dto.setPoints(grossPoints);
            dto.setNetPoints(netPoints);
            dto.setAverageScore(gw.getAverageEntryScore());
            dto.setHighestScore(gw.getHighestScore());
            dto.setRank(st.getRank());
            dto.setOverallRank(st.getOverallRank());
            dto.setOverallPoints(st.getOverallPoints());
            dto.setTransfers(transfers);
            dto.setTransferCost(cost);
            dto.setPointsOnBench(benchPts);
            dto.setBank(st.getBank() != null ? st.getBank() / 10.0 : null);
            dto.setTeamValue(st.getTeamValue() != null ? st.getTeamValue() / 10.0 : null);
            dto.setChipUsed(chipUsed);
            dto.setCaptainName(captainName);
            dto.setCaptainPoints(captainEffectivePts);
            gwList.add(dto);
        }

        // Summary KPI calculations
        PerformanceHistoryResponse.SeasonKpiDto kpi = new PerformanceHistoryResponse.SeasonKpiDto();
        MyTeamState latest = states.isEmpty() ? null : states.get(states.size() - 1);

        if (latest != null) {
            kpi.setTotalPoints(latest.getOverallPoints() != null ? latest.getOverallPoints() : (int) totalNetPoints);
            kpi.setCurrentOverallRank(latest.getOverallRank());
            kpi.setTeamValue(latest.getTeamValue() != null ? latest.getTeamValue() / 10.0 : 100.0);
            kpi.setBank(latest.getBank() != null ? latest.getBank() / 10.0 : 0.0);
        }

        if (bestRank != Integer.MAX_VALUE) {
            kpi.setBestGameweekRank(bestRank);
            kpi.setBestGameweekRankEvent(bestRankGw);
        }

        double avgGwScore = validGwCount > 0 ? (totalNetPoints / validGwCount) : 0;
        double avgLeagueScore = validGwCount > 0 ? (totalLeagueAvgPoints / validGwCount) : 0;
        kpi.setAverageGameweekScore(Math.round(avgGwScore * 10.0) / 10.0);
        kpi.setAverageLeagueScore(Math.round(avgLeagueScore * 10.0) / 10.0);
        kpi.setScoreVersusLeagueAvg(Math.round((avgGwScore - avgLeagueScore) * 10.0) / 10.0);

        kpi.setTotalTransfers(totalTransfers);
        kpi.setTotalTransferCost(totalTransferCost);
        kpi.setTotalBenchPoints(totalBenchPoints);

        PerformanceHistoryResponse.CaptainEfficiencyDto capEff = new PerformanceHistoryResponse.CaptainEfficiencyDto();
        capEff.setTotalCaptainPoints(totalCaptainPoints);
        capEff.setTotalGameweeks(validGwCount);
        capEff.setOptimalCaptainHits(optimalCaptainHits);
        capEff.setAverageCaptainPoints(validGwCount > 0 ? Math.round((totalCaptainPoints / (double) validGwCount) * 10.0) / 10.0 : 0.0);
        kpi.setCaptainEfficiency(capEff);

        // Generate deterministic insights
        List<String> insights = new ArrayList<>();
        if (validGwCount > 0) {
            if (bestRank != Integer.MAX_VALUE) {
                insights.add(String.format("🌟 Best Gameweek: GW %d with rank %,d.", bestRankGw, bestRank));
            }
            double diff = Math.round((avgGwScore - avgLeagueScore) * 10.0) / 10.0;
            String diffPrefix = diff >= 0 ? "+" : "";
            insights.add(String.format("📊 Average Score: %.1f pts/GW vs league average of %.1f (%s%.1f pts/GW difference).",
                    avgGwScore, avgLeagueScore, diffPrefix, diff));
            insights.add(String.format("👑 Captaincy Impact: Captain picks earned %d points (avg %.1f pts/GW), scoring as the highest starter in %d of %d gameweeks.",
                    totalCaptainPoints, capEff.getAverageCaptainPoints(), optimalCaptainHits, validGwCount));
            if (totalBenchPoints > 0) {
                insights.add(String.format("🪑 Bench Management: %d total points left on bench, with a season high of %d points in GW %d.",
                        totalBenchPoints, maxBenchPoints, maxBenchGw));
            }
            if (totalTransferCost > 0) {
                insights.add(String.format("🔄 Transfer Audit: %d transfers made with %d points deducted in transfer hits.",
                        totalTransfers, totalTransferCost));
            } else {
                insights.add(String.format("🔄 Transfer Discipline: %d transfers made with zero points lost to transfer hits.",
                        totalTransfers));
            }
        }

        PerformanceHistoryResponse response = new PerformanceHistoryResponse();
        response.setSummary(kpi);
        response.setGameweeks(gwList);
        response.setChipsTimeline(chipsList);
        response.setDeterministicInsights(insights);
        return response;
    }

    private String mapChipName(String chip) {
        if (chip == null) return "None";
        return switch (chip.toLowerCase()) {
            case "3xc" -> "Triple Captain";
            case "bboost" -> "Bench Boost";
            case "freehit" -> "Free Hit";
            case "wildcard" -> "Wildcard";
            default -> chip;
        };
    }
}
