package com.fplassistant.service;

import com.fplassistant.dto.TeamCurrentResponse;
import com.fplassistant.exception.ResourceNotFoundException;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final MyTeamStateRepository myTeamStateRepo;
    private final MyTeamPickRepository myTeamPickRepo;
    private final GameweekRepository gameweekRepo;
    private final PlayerGameweekStatsRepository statsRepo;
    private final PredictionRepository predictionRepo;

    public TeamService(MyTeamStateRepository myTeamStateRepo,
                       MyTeamPickRepository myTeamPickRepo,
                       GameweekRepository gameweekRepo,
                       PlayerGameweekStatsRepository statsRepo,
                       PredictionRepository predictionRepo) {
        this.myTeamStateRepo = myTeamStateRepo;
        this.myTeamPickRepo = myTeamPickRepo;
        this.gameweekRepo = gameweekRepo;
        this.statsRepo = statsRepo;
        this.predictionRepo = predictionRepo;
    }

    public TeamCurrentResponse getCurrentTeam() {
        return getCurrentTeam(null);
    }

    /**
     * Returns the squad (XI + bench), badge URLs, live event points, and expected points for specified or current GW.
     */
    @Transactional(readOnly = true)
    public TeamCurrentResponse getCurrentTeam(Integer targetGw) {
        // Resolve current and next gameweeks
        Gameweek currentGwObj = gameweekRepo.findByIsCurrentTrue()
                .or(() -> gameweekRepo.findByIsNextTrue()
                .or(() -> gameweekRepo.findById(1)))
                .or(() -> gameweekRepo.findAll().stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No gameweeks found. Run POST /api/team/sync first."));

        Gameweek nextGwObj = gameweekRepo.findByIsNextTrue()
                .orElse(currentGwObj);

        int selectedGwId = targetGw != null ? targetGw : currentGwObj.getId();

        // Get team state for selected GW (or latest state)
        MyTeamState state = myTeamStateRepo.findByGameweekId(selectedGwId)
                .or(() -> myTeamStateRepo.findTopByOrderByGameweekIdDesc())
                .orElseThrow(() -> new ResourceNotFoundException("No team state found. Run POST /api/team/sync first."));

        List<MyTeamPick> picks = myTeamPickRepo.findByGameweekIdOrderByPositionAsc(state.getGameweek().getId());
        if (picks.isEmpty()) {
            // Fall back to latest available picks
            MyTeamState latestState = myTeamStateRepo.findTopByOrderByGameweekIdDesc().orElse(state);
            picks = myTeamPickRepo.findByGameweekIdOrderByPositionAsc(latestState.getGameweek().getId());
        }

        // Fetch stats for live points calculation
        Map<Integer, PlayerGameweekStats> statsMap = statsRepo.findByGameweekId(selectedGwId)
                .stream()
                .collect(Collectors.toMap(s -> s.getPlayer().getId(), s -> s, (a, b) -> a));

        // Fetch predictions for expected points
        Map<Integer, Prediction> predMap = predictionRepo.findByGameweekId(selectedGwId)
                .stream()
                .collect(Collectors.toMap(p -> p.getPlayer().getId(), p -> p, (a, b) -> a));

        List<TeamCurrentResponse.PickDto> startingXI = new ArrayList<>();
        List<TeamCurrentResponse.PickDto> bench = new ArrayList<>();
        int liveTotal = 0;

        for (MyTeamPick pick : picks) {
            Player p = pick.getPlayer();
            Team t = p.getTeam();

            // Generate official Premier League SVG badge URL
            Integer teamCode = t != null ? t.getCode() : 3;
            String badgeUrl = "https://resources.premierleague.com/premierleague25/badges-alt/" + teamCode + ".svg";

            // Live event points calculation
            PlayerGameweekStats stats = statsMap.get(p.getId());
            int rawPoints = stats != null && stats.getTotalPoints() != null ? stats.getTotalPoints() : 0;
            int multiplier = pick.getMultiplier() != null ? pick.getMultiplier().intValue() : 1;
            int eventPointsScored = rawPoints * multiplier;

            // Prediction xPoints
            Prediction pred = predMap.get(p.getId());
            BigDecimal xPoints = pred != null ? pred.getExpectedPoints() : p.getForm();

            TeamCurrentResponse.PickDto dto = TeamCurrentResponse.PickDto.builder()
                    .playerId(p.getId())
                    .webName(p.getWebName())
                    .teamShortName(t != null ? t.getShortName() : "???")
                    .teamCode(teamCode)
                    .badgeUrl(badgeUrl)
                    .position(mapElementType(p.getElementType()))
                    .squadPosition(pick.getPosition() != null ? pick.getPosition().intValue() : 1)
                    .multiplier(multiplier)
                    .isCaptain(pick.getIsCaptain() != null ? pick.getIsCaptain() : false)
                    .isViceCaptain(pick.getIsViceCaptain() != null ? pick.getIsViceCaptain() : false)
                    .nowCost(p.getNowCost() != null ? (int) p.getNowCost() : 50)
                    .form(p.getForm())
                    .totalPoints(p.getTotalPoints())
                    .eventPoints(eventPointsScored)
                    .expectedPoints(xPoints)
                    .status(p.getStatus())
                    .news(p.getNews())
                    .build();

            if (pick.getPosition() <= 11) {
                startingXI.add(dto);
                liveTotal += eventPointsScored;
            } else {
                bench.add(dto);
            }
        }

        TeamCurrentResponse.ChipsAvailable chips = TeamCurrentResponse.ChipsAvailable.builder()
                .wildcard(state.getWildcardAvailable())
                .freeHit(state.getFreeHitAvailable())
                .benchBoost(state.getBenchBoostAvailable())
                .tripleCaptain(state.getTripleCaptainAvailable())
                .build();

        return TeamCurrentResponse.builder()
                .gameweekId(selectedGwId)
                .currentGameweekId(currentGwObj.getId())
                .nextGameweekId(nextGwObj.getId())
                .overallRank(state.getOverallRank())
                .overallPoints(state.getOverallPoints())
                .gameweekPoints(state.getGameweekPoints())
                .liveTotalPoints(liveTotal)
                .bank(state.getBank())
                .teamValue(state.getTeamValue())
                .freeTransfers(state.getFreeTransfers())
                .chips(chips)
                .startingXI(startingXI)
                .bench(bench)
                .build();
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
}
