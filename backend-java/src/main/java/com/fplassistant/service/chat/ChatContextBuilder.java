package com.fplassistant.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fplassistant.dto.TeamCurrentResponse;
import com.fplassistant.model.*;
import com.fplassistant.repository.FixtureRepository;
import com.fplassistant.repository.PredictionRepository;
import com.fplassistant.repository.TeamRepository;
import com.fplassistant.service.RecommendationService;
import com.fplassistant.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Component
public class ChatContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(ChatContextBuilder.class);

    private final TeamService teamService;
    private final RecommendationService recommendationService;
    private final PredictionRepository predictionRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamRepository teamRepository;
    private final ObjectMapper objectMapper;

    public ChatContextBuilder(TeamService teamService,
                              RecommendationService recommendationService,
                              PredictionRepository predictionRepository,
                              FixtureRepository fixtureRepository,
                              TeamRepository teamRepository,
                              ObjectMapper objectMapper) {
        this.teamService = teamService;
        this.recommendationService = recommendationService;
        this.predictionRepository = predictionRepository;
        this.fixtureRepository = fixtureRepository;
        this.teamRepository = teamRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildContext(Intent intent, List<Player> resolvedPlayers) {
        Map<String, Object> context = new LinkedHashMap<>();
        Instant now = Instant.now();
        context.put("dataAsOf", now.toString());

        // 1. Fetch current squad state
        TeamCurrentResponse squad = null;
        try {
            squad = teamService.getCurrentTeam();
        } catch (Exception e) {
            log.warn("Could not load current team for chat context: {}", e.getMessage());
        }

        int nextGw = (squad != null && squad.getNextGameweekId() != null) ? squad.getNextGameweekId() : 9;
        int currentGw = (squad != null && squad.getCurrentGameweekId() != null) ? squad.getCurrentGameweekId() : 8;

        context.put("gameweek_info", Map.of(
                "current_gameweek", currentGw,
                "next_gameweek", nextGw
        ));

        // 2. Base Squad & Chips info
        if (squad != null) {
            List<String> chipsRemaining = new ArrayList<>();
            List<String> chipsUsed = new ArrayList<>();

            if (squad.getChips() != null) {
                if (Boolean.TRUE.equals(squad.getChips().getWildcard())) chipsRemaining.add("Wildcard"); else chipsUsed.add("Wildcard");
                if (Boolean.TRUE.equals(squad.getChips().getFreeHit())) chipsRemaining.add("Free Hit"); else chipsUsed.add("Free Hit");
                if (Boolean.TRUE.equals(squad.getChips().getBenchBoost())) chipsRemaining.add("Bench Boost"); else chipsUsed.add("Bench Boost");
                if (Boolean.TRUE.equals(squad.getChips().getTripleCaptain())) chipsRemaining.add("Triple Captain"); else chipsUsed.add("Triple Captain");
            }

            double bankM = squad.getBank() != null ? squad.getBank() / 10.0 : 0.0;
            double teamValM = squad.getTeamValue() != null ? squad.getTeamValue() / 10.0 : 100.0;

            Map<String, Object> squadSummary = new LinkedHashMap<>();
            squadSummary.put("overall_rank", squad.getOverallRank() != null ? squad.getOverallRank() : 0);
            squadSummary.put("overall_points", squad.getOverallPoints() != null ? squad.getOverallPoints() : 0);
            squadSummary.put("bank_millions", round(bankM, 1));
            squadSummary.put("team_value_millions", round(teamValM, 1));
            squadSummary.put("free_transfers", squad.getFreeTransfers() != null ? squad.getFreeTransfers() : 1);
            squadSummary.put("chips_remaining", chipsRemaining);
            squadSummary.put("chips_used", chipsUsed);
            context.put("squad_summary", squadSummary);

            // Starting XI compact list
            if (squad.getStartingXI() != null) {
                List<Map<String, Object>> starting = new ArrayList<>();
                for (TeamCurrentResponse.PickDto p : squad.getStartingXI()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", p.getWebName());
                    item.put("pos", p.getPosition());
                    item.put("team", p.getTeamShortName());
                    item.put("cost_m", p.getNowCost() != null ? round(p.getNowCost() / 10.0, 1) : 0.0);
                    item.put("xP_next", p.getExpectedPoints() != null ? round(p.getExpectedPoints().doubleValue(), 2) : 0.0);
                    item.put("is_captain", Boolean.TRUE.equals(p.getIsCaptain()));
                    item.put("is_vice", Boolean.TRUE.equals(p.getIsViceCaptain()));
                    item.put("status", p.getStatus() != null ? p.getStatus() : "a");
                    starting.add(item);
                }
                context.put("starting_xi", starting);
            }

            // Bench compact list
            if (squad.getBench() != null) {
                List<Map<String, Object>> bench = new ArrayList<>();
                for (TeamCurrentResponse.PickDto p : squad.getBench()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", p.getWebName());
                    item.put("pos", p.getPosition());
                    item.put("team", p.getTeamShortName());
                    item.put("cost_m", p.getNowCost() != null ? round(p.getNowCost() / 10.0, 1) : 0.0);
                    item.put("xP_next", p.getExpectedPoints() != null ? round(p.getExpectedPoints().doubleValue(), 2) : 0.0);
                    item.put("status", p.getStatus() != null ? p.getStatus() : "a");
                    bench.add(item);
                }
                context.put("bench", bench);
            }
        }

        // 3. Intent-Specific Context Blocks
        switch (intent) {
            case CHIPS_STATUS, CHIP_STRATEGY -> {
                try {
                    Map<String, Object> strategy = recommendationService.getChipStrategy(8);
                    if (strategy != null) {
                        context.put("chip_recommendations", strategy);
                    }
                } catch (Exception e) {
                    log.debug("Chip strategy service unreachable: {}", e.getMessage());
                }
            }
            case TRANSFER_ADVICE -> {
                try {
                    Map<String, Object> transfers = recommendationService.getTransferRecommendations(3, 5);
                    if (transfers != null) {
                        Object recs = transfers.getOrDefault("recommendations", transfers.get("top_transfers"));
                        if (recs != null) {
                            context.put("top_transfer_recommendations", recs);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Transfer recommendations unavailable: {}", e.getMessage());
                }
            }
            case CAPTAIN_ADVICE -> {
                try {
                    Map<String, Object> captains = recommendationService.getCaptainRecommendations("balanced");
                    if (captains != null) {
                        Object r = captains.getOrDefault("rankings", captains.get("captaincy_rankings"));
                        if (r != null) {
                            context.put("captaincy_rankings", r);
                        }
                        if (captains.containsKey("top_recommendation")) {
                            context.put("top_recommendation", captains.get("top_recommendation"));
                        }
                        if (captains.containsKey("vice_captain_suggestion")) {
                            context.put("vice_captain_suggestion", captains.get("vice_captain_suggestion"));
                        }
                    }
                } catch (Exception e) {
                    log.debug("Captain recommendations unavailable: {}", e.getMessage());
                }
            }
            case SQUAD_WEAKNESS -> {
                try {
                    Map<String, Object> weaknesses = recommendationService.getSquadWeaknesses();
                    if (weaknesses != null) {
                        context.put("squad_weaknesses", weaknesses);
                    }
                } catch (Exception e) {
                    log.debug("Squad weaknesses unavailable: {}", e.getMessage());
                }
            }
            case DIFFERENTIALS -> {
                try {
                    Map<String, Object> differentials = recommendationService.getDifferentials(10.0);
                    if (differentials != null && differentials.containsKey("differentials")) {
                        context.put("differentials", differentials.get("differentials"));
                    }
                } catch (Exception e) {
                    log.debug("Differentials unavailable: {}", e.getMessage());
                }
            }
            case PLAYER_COMPARE, PLAYER_INFO -> {
                if (resolvedPlayers != null && !resolvedPlayers.isEmpty()) {
                    List<Map<String, Object>> pDetails = new ArrayList<>();
                    for (Player p : resolvedPlayers) {
                        Map<String, Object> pInfo = new LinkedHashMap<>();
                        pInfo.put("id", p.getId());
                        pInfo.put("name", p.getWebName());
                        pInfo.put("full_name", p.getFirstName() + " " + p.getSecondName());
                        pInfo.put("position", mapElementType(p.getElementType()));
                        pInfo.put("cost_m", p.getNowCost() != null ? round(p.getNowCost() / 10.0, 1) : 0.0);
                        pInfo.put("form", p.getForm() != null ? round(p.getForm().doubleValue(), 1) : 0.0);
                        pInfo.put("total_points", p.getTotalPoints() != null ? p.getTotalPoints() : 0);
                        pInfo.put("minutes", p.getMinutes() != null ? p.getMinutes() : 0);
                        pInfo.put("status", p.getStatus() != null ? p.getStatus() : "a");
                        pInfo.put("news", p.getNews() != null ? p.getNews() : "");

                        // xPoints for next gameweek
                        List<Prediction> preds = predictionRepository.findByPlayerIdAndGameweekId(p.getId(), nextGw);
                        if (!preds.isEmpty()) {
                            Prediction pred = preds.get(0);
                            pInfo.put("xP_next", pred.getExpectedPoints() != null ? round(pred.getExpectedPoints().doubleValue(), 2) : 0.0);
                        } else {
                            pInfo.put("xP_next", p.getForm() != null ? round(p.getForm().doubleValue(), 2) : 3.0);
                        }
                        pDetails.add(pInfo);
                    }
                    context.put("target_players", pDetails);
                }
            }
            case FIXTURE_ANALYSIS -> {
                try {
                    List<Fixture> upcoming = fixtureRepository.findByGameweekIdBetween(nextGw, nextGw + 3);
                    List<Map<String, Object>> fixList = new ArrayList<>();
                    for (Fixture f : upcoming) {
                        Map<String, Object> fMap = new LinkedHashMap<>();
                        fMap.put("gameweek", f.getGameweek() != null ? f.getGameweek().getId() : nextGw);
                        fMap.put("home_team_id", f.getTeamHome() != null ? f.getTeamHome().getId() : null);
                        fMap.put("away_team_id", f.getTeamAway() != null ? f.getTeamAway().getId() : null);
                        fMap.put("home_fdr", f.getTeamHomeDifficulty());
                        fMap.put("away_fdr", f.getTeamAwayDifficulty());
                        fixList.add(fMap);
                        if (fixList.size() >= 15) break;
                    }
                    context.put("upcoming_fixtures", fixList);
                } catch (Exception e) {
                    log.debug("Fixture repository query failed: {}", e.getMessage());
                }
            }
            default -> {
                // GENERAL_FPL or OFF_TOPIC: squad summary is sufficient
            }
        }

        return context;
    }

    public String toJson(Map<String, Object> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            log.error("Failed to serialize chat context to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private double round(double val, int decimals) {
        return BigDecimal.valueOf(val).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    private String mapElementType(Short type) {
        if (type == null) return "MID";
        return switch (type) {
            case 1 -> "GK";
            case 2 -> "DEF";
            case 3 -> "MID";
            case 4 -> "FWD";
            default -> "MID";
        };
    }
}
