package com.fplassistant.service;

import com.fplassistant.dto.TeamCurrentResponse;
import com.fplassistant.exception.ResourceNotFoundException;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameweekSquadService {

    private static final Logger log = LoggerFactory.getLogger(GameweekSquadService.class);

    private final MyTeamStateRepository myTeamStateRepo;
    private final MyTeamPickRepository myTeamPickRepo;
    private final MyTeamAutoSubRepository myTeamAutoSubRepo;
    private final GameweekRepository gameweekRepo;
    private final PlayerGameweekStatsRepository statsRepo;
    private final PredictionRepository predictionRepo;
    private final TransferLogRepository transferLogRepo;
    private final GameweekPhaseService phaseService;

    public GameweekSquadService(MyTeamStateRepository myTeamStateRepo,
                                MyTeamPickRepository myTeamPickRepo,
                                MyTeamAutoSubRepository myTeamAutoSubRepo,
                                GameweekRepository gameweekRepo,
                                PlayerGameweekStatsRepository statsRepo,
                                PredictionRepository predictionRepo,
                                TransferLogRepository transferLogRepo,
                                GameweekPhaseService phaseService) {
        this.myTeamStateRepo = myTeamStateRepo;
        this.myTeamPickRepo = myTeamPickRepo;
        this.myTeamAutoSubRepo = myTeamAutoSubRepo;
        this.gameweekRepo = gameweekRepo;
        this.statsRepo = statsRepo;
        this.predictionRepo = predictionRepo;
        this.transferLogRepo = transferLogRepo;
        this.phaseService = phaseService;
    }

    @Transactional(readOnly = true)
    public TeamCurrentResponse getSquadForGameweek(Integer targetGw) {
        int selectedGwId = targetGw != null ? targetGw : phaseService.getDefaultHomeGameweekId();

        Gameweek gw = gameweekRepo.findById(selectedGwId)
                .orElseThrow(() -> new ResourceNotFoundException("Gameweek " + selectedGwId + " not found."));

        Gameweek currentGwObj = gameweekRepo.findByIsCurrentTrue()
                .or(() -> gameweekRepo.findById(1))
                .orElse(gw);

        Gameweek nextGwObj = gameweekRepo.findByIsNextTrue()
                .orElse(currentGwObj);

        String phase = phaseService.determinePhase(gw);

        // Retrieve team state for this GW, or latest state if pre-deadline/upcoming
        MyTeamState state = myTeamStateRepo.findByGameweekId(selectedGwId).orElse(null);
        MyTeamState latestState = myTeamStateRepo.findTopByOrderByGameweekIdDesc().orElse(null);
        MyTeamState effectiveState = state != null ? state : latestState;

        // Squad resolution: Check if DB has picks for this GW
        List<MyTeamPick> dbPicks = myTeamPickRepo.findByGameweekIdOrderByPositionAsc(selectedGwId);
        boolean isProjected = false;
        List<PickResolution> resolvedPicks = new ArrayList<>();

        if (!dbPicks.isEmpty()) {
            // Settled or recorded picks exist in DB
            for (MyTeamPick p : dbPicks) {
                resolvedPicks.add(new PickResolution(
                        p.getPlayer(),
                        p.getPosition() != null ? p.getPosition().intValue() : 1,
                        p.getMultiplier() != null ? p.getMultiplier().intValue() : 1,
                        Boolean.TRUE.equals(p.getIsCaptain()),
                        Boolean.TRUE.equals(p.getIsViceCaptain())
                ));
            }
        } else {
            // Projected squad on-the-fly (NEVER persist to DB)
            isProjected = true;
            resolvedPicks = projectUpcomingSquad(selectedGwId);
        }

        // Auto-subs mapping
        Map<Integer, String> subInMap = new HashMap<>();
        Map<Integer, String> subOutMap = new HashMap<>();
        if (!isProjected) {
            List<MyTeamAutoSub> autoSubs = myTeamAutoSubRepo.findByGameweekId(selectedGwId);
            for (MyTeamAutoSub as : autoSubs) {
                if (as.getPlayerIn() != null && as.getPlayerOut() != null) {
                    subInMap.put(as.getPlayerIn().getId(), as.getPlayerOut().getWebName());
                    subOutMap.put(as.getPlayerOut().getId(), as.getPlayerIn().getWebName());
                }
            }
        }

        // Stats & predictions
        Map<Integer, PlayerGameweekStats> statsMap = statsRepo.findByGameweekId(selectedGwId)
                .stream()
                .collect(Collectors.toMap(s -> s.getPlayer().getId(), s -> s, (a, b) -> a));

        Map<Integer, Prediction> predMap = predictionRepo.findByGameweekId(selectedGwId)
                .stream()
                .collect(Collectors.toMap(p -> p.getPlayer().getId(), p -> p, (a, b) -> a));

        List<TeamCurrentResponse.PickDto> startingXI = new ArrayList<>();
        List<TeamCurrentResponse.PickDto> bench = new ArrayList<>();

        for (PickResolution res : resolvedPicks) {
            Player p = res.player;
            Team t = p.getTeam();
            Integer teamCode = t != null ? t.getCode() : 3;
            String badgeUrl = "https://resources.premierleague.com/premierleague25/badges-alt/" + teamCode + ".svg";

            PlayerGameweekStats stats = statsMap.get(p.getId());
            int rawPoints = (stats != null && stats.getTotalPoints() != null) ? stats.getTotalPoints() : 0;
            int effectivePoints = rawPoints * res.multiplier;

            Prediction pred = predMap.get(p.getId());
            BigDecimal xPoints = pred != null ? pred.getExpectedPoints() : p.getForm();

            boolean isSubIn = subInMap.containsKey(p.getId());
            boolean isSubOut = subOutMap.containsKey(p.getId());
            String subbedWith = isSubIn ? subInMap.get(p.getId()) : (isSubOut ? subOutMap.get(p.getId()) : null);

            TeamCurrentResponse.PickDto.Builder dtoBuilder = TeamCurrentResponse.PickDto.builder()
                    .playerId(p.getId())
                    .webName(p.getWebName())
                    .teamShortName(t != null ? t.getShortName() : "???")
                    .teamCode(teamCode)
                    .badgeUrl(badgeUrl)
                    .position(mapElementType(p.getElementType()))
                    .squadPosition(res.position)
                    .multiplier(res.multiplier)
                    .isCaptain(res.isCaptain)
                    .isViceCaptain(res.isViceCaptain)
                    .nowCost(p.getNowCost() != null ? (int) p.getNowCost() : 50)
                    .form(p.getForm())
                    .totalPoints(p.getTotalPoints())
                    .eventPoints(effectivePoints)
                    .rawPoints(rawPoints)
                    .effectivePoints(effectivePoints)
                    .expectedPoints(xPoints)
                    .status(p.getStatus())
                    .news(p.getNews())
                    .isAutoSubIn(isSubIn)
                    .isAutoSubOut(isSubOut)
                    .subbedWithPlayerName(subbedWith);

            if (stats != null) {
                dtoBuilder.minutes(stats.getMinutes())
                        .goals(stats.getGoalsScored())
                        .assists(stats.getAssists())
                        .cleanSheets(stats.getCleanSheets())
                        .bonus(stats.getBonus())
                        .bps(stats.getBps())
                        .yellowCards(stats.getYellowCards())
                        .redCards(stats.getRedCards());
            } else {
                dtoBuilder.minutes(0).goals(0).assists(0).cleanSheets(0).bonus(0).bps(0).yellowCards(0).redCards(0);
            }

            TeamCurrentResponse.PickDto dto = dtoBuilder.build();
            if (res.position <= 11) {
                startingXI.add(dto);
            } else {
                bench.add(dto);
            }
        }

        // Totals & reconciliation
        int startingSum = startingXI.stream().mapToInt(p -> p.getEffectivePoints() != null ? p.getEffectivePoints() : 0).sum();
        int benchSum = bench.stream().mapToInt(p -> p.getRawPoints() != null ? p.getRawPoints() : 0).sum();

        boolean isBenchBoost = (state != null && (Boolean.TRUE.equals(state.getBenchBoostActive()) || "bboost".equalsIgnoreCase(state.getActiveChip())));
        if (isBenchBoost) {
            startingSum += benchSum;
        }

        Integer stateGrossPoints = state != null ? state.getGameweekPoints() : null;
        Integer officialGross = stateGrossPoints != null ? stateGrossPoints : startingSum;
        int transfersCost = (state != null && state.getEventTransfersCost() != null) ? state.getEventTransfersCost() : 0;
        int netPoints = officialGross - transfersCost;

        boolean pointsDiscrepancy = false;
        if ("FINISHED".equals(phase) && stateGrossPoints != null && !stateGrossPoints.equals(startingSum)) {
            log.warn("GW {} points discrepancy: entry_history={} vs calculated sum={}", selectedGwId, stateGrossPoints, startingSum);
            pointsDiscrepancy = true;
        }

        TeamCurrentResponse.ChipsAvailable chips = null;
        if (effectiveState != null) {
            chips = TeamCurrentResponse.ChipsAvailable.builder()
                    .wildcard(effectiveState.getWildcardAvailable())
                    .freeHit(effectiveState.getFreeHitAvailable())
                    .benchBoost(effectiveState.getBenchBoostAvailable())
                    .tripleCaptain(effectiveState.getTripleCaptainAvailable())
                    .build();
        }

        return TeamCurrentResponse.builder()
                .gameweekId(selectedGwId)
                .currentGameweekId(currentGwObj.getId())
                .nextGameweekId(nextGwObj.getId())
                .phase(phase)
                .isProjected(isProjected)
                .overallRank(state != null ? state.getOverallRank() : (effectiveState != null ? effectiveState.getOverallRank() : null))
                .overallPoints(state != null ? state.getOverallPoints() : (effectiveState != null ? effectiveState.getOverallPoints() : null))
                .gameweekPoints(officialGross)
                .gameweekNetPoints(netPoints)
                .transfersCost(transfersCost)
                .pointsOnBench(state != null && state.getPointsOnBench() != null ? state.getPointsOnBench() : benchSum)
                .liveTotalPoints(startingSum)
                .reconciledGameweekPoints(startingSum)
                .pointsDiscrepancy(pointsDiscrepancy)
                .averageEntryScore(gw.getAverageEntryScore())
                .highestScore(gw.getHighestScore())
                .activeChip(state != null ? state.getActiveChip() : null)
                .bank(effectiveState != null ? effectiveState.getBank() : null)
                .teamValue(effectiveState != null ? effectiveState.getTeamValue() : null)
                .freeTransfers(effectiveState != null ? effectiveState.getFreeTransfers() : 1)
                .chips(chips)
                .startingXI(startingXI)
                .bench(bench)
                .build();
    }

    /**
     * Projects upcoming squad by taking the latest non-FreeHit settled squad and applying confirmed transfers.
     */
    private List<PickResolution> projectUpcomingSquad(int upcomingGwId) {
        List<MyTeamState> statesDesc = myTeamStateRepo.findAll(Sort.by(Sort.Direction.DESC, "gameweek.id"));
        int baseGwId = 1;
        for (MyTeamState st : statesDesc) {
            // Skip free hit gameweeks as picks revert next week
            if (Boolean.TRUE.equals(st.getFreeHitActive()) || "freehit".equalsIgnoreCase(st.getActiveChip())) {
                continue;
            }
            if (myTeamPickRepo.countByGameweekId(st.getGameweek().getId()) >= 15) {
                baseGwId = st.getGameweek().getId();
                break;
            }
        }

        List<MyTeamPick> basePicks = myTeamPickRepo.findByGameweekIdOrderByPositionAsc(baseGwId);
        List<PickResolution> result = new ArrayList<>();
        for (MyTeamPick p : basePicks) {
            result.add(new PickResolution(
                    p.getPlayer(),
                    p.getPosition() != null ? p.getPosition().intValue() : 1,
                    p.getMultiplier() != null ? p.getMultiplier().intValue() : 1,
                    Boolean.TRUE.equals(p.getIsCaptain()),
                    Boolean.TRUE.equals(p.getIsViceCaptain())
            ));
        }

        // Apply confirmed transfers for upcomingGwId (if any logged in transfers_log)
        List<TransferLog> transfers = transferLogRepo.findByGameweekIdOrderByTransferTimeDesc(upcomingGwId);
        for (TransferLog t : transfers) {
            if (t.getPlayerIn() != null && t.getPlayerOut() != null) {
                for (PickResolution pr : result) {
                    if (pr.player.getId().equals(t.getPlayerOut().getId())) {
                        pr.player = t.getPlayerIn();
                        break;
                    }
                }
            }
        }

        return result;
    }

    private String mapElementType(Number type) {
        if (type == null) return "MID";
        return switch (type.intValue()) {
            case 1 -> "GK";
            case 2 -> "DEF";
            case 3 -> "MID";
            case 4 -> "FWD";
            default -> "MID";
        };
    }

    private static class PickResolution {
        Player player;
        int position;
        int multiplier;
        boolean isCaptain;
        boolean isViceCaptain;

        PickResolution(Player player, int position, int multiplier, boolean isCaptain, boolean isViceCaptain) {
            this.player = player;
            this.position = position;
            this.multiplier = multiplier;
            this.isCaptain = isCaptain;
            this.isViceCaptain = isViceCaptain;
        }
    }
}
