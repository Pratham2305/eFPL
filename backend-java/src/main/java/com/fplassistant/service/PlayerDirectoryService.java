package com.fplassistant.service;

import com.fplassistant.dto.PlayerDetailDto;
import com.fplassistant.dto.PlayerMetaDto;
import com.fplassistant.dto.PlayerPageResponse;
import com.fplassistant.dto.PlayerSummaryDto;
import com.fplassistant.exception.ResourceNotFoundException;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import com.fplassistant.service.chat.PlayerResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlayerDirectoryService {

    private static final Logger log = LoggerFactory.getLogger(PlayerDirectoryService.class);

    private final PlayerRepository playerRepo;
    private final TeamRepository teamRepo;
    private final GameweekRepository gameweekRepo;
    private final FixtureRepository fixtureRepo;
    private final PredictionRepository predictionRepo;
    private final PlayerGameweekStatsRepository playerGameweekStatsRepo;
    private final MyTeamStateRepository myTeamStateRepo;
    private final MyTeamPickRepository myTeamPickRepo;

    public PlayerDirectoryService(PlayerRepository playerRepo,
                                  TeamRepository teamRepo,
                                  GameweekRepository gameweekRepo,
                                  FixtureRepository fixtureRepo,
                                  PredictionRepository predictionRepo,
                                  PlayerGameweekStatsRepository playerGameweekStatsRepo,
                                  MyTeamStateRepository myTeamStateRepo,
                                  MyTeamPickRepository myTeamPickRepo) {
        this.playerRepo = playerRepo;
        this.teamRepo = teamRepo;
        this.gameweekRepo = gameweekRepo;
        this.fixtureRepo = fixtureRepo;
        this.predictionRepo = predictionRepo;
        this.playerGameweekStatsRepo = playerGameweekStatsRepo;
        this.myTeamStateRepo = myTeamStateRepo;
        this.myTeamPickRepo = myTeamPickRepo;
    }

    @Transactional(readOnly = true)
    public PlayerPageResponse getPlayers(String search,
                                         List<Integer> teams,
                                         List<Short> positions,
                                         Double minPrice,
                                         Double maxPrice,
                                         List<String> status,
                                         Double minOwnership,
                                         Double maxOwnership,
                                         Boolean inSquad,
                                         String sortBy,
                                         String sortDir,
                                         int page,
                                         int size) {
        // 1. Validation
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("minPrice must be non-negative");
        }
        if (maxPrice != null && minPrice != null && maxPrice < minPrice) {
            throw new IllegalArgumentException("maxPrice cannot be less than minPrice");
        }

        String actualSortBy = (sortBy == null || sortBy.isBlank()) ? "total_points" : sortBy.trim();
        String actualSortDir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir.trim().toLowerCase();
        if (!actualSortDir.equals("asc") && !actualSortDir.equals("desc")) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDir + ". Must be 'asc' or 'desc'");
        }

        // 2. Resolve Context: Gameweeks, Next Fixtures, Predictions, User Squad
        Gameweek currentGw = resolveCurrentGameweek();
        Gameweek nextGw = resolveNextGameweek(currentGw);
        int nextGwId = nextGw != null ? nextGw.getId() : (currentGw != null ? currentGw.getId() : 1);

        Map<Integer, List<Fixture>> nextFixturesByTeam = getFixturesForGameweek(nextGwId);
        Map<Integer, Prediction> nextPredictionsByPlayer = getPredictionsForGameweek(nextGwId);
        Map<Integer, MyTeamPick> userPicksMap = getUserSquadPicks(currentGw != null ? currentGw.getId() : nextGwId);
        Map<Integer, Team> teamMap = teamRepo.findAll().stream()
                .collect(Collectors.toMap(Team::getId, t -> t, (a, b) -> a));

        List<Player> allPlayers = playerRepo.findAll();

        // 3. Build Summaries
        List<PlayerSummaryDto> dtos = new ArrayList<>(allPlayers.size());
        for (Player p : allPlayers) {
            Team t = p.getTeam() != null ? p.getTeam() : teamMap.get(p.getTeam() != null ? p.getTeam().getId() : null);
            dtos.add(toSummaryDto(p, t, nextGwId, nextFixturesByTeam, nextPredictionsByPlayer, userPicksMap));
        }

        // 4. Filtering
        List<PlayerSummaryDto> filtered = dtos.stream().filter(p -> {
            // Search filter with token matching and accent normalization
            if (search != null && !search.isBlank()) {
                String normalizedSearch = PlayerResolver.normalize(search);
                String[] tokens = normalizedSearch.split("\\s+");
                String haystack = PlayerResolver.normalize(
                        (p.getFirstName() != null ? p.getFirstName() : "") + " "
                        + (p.getSecondName() != null ? p.getSecondName() : "") + " "
                        + (p.getWebName() != null ? p.getWebName() : "") + " "
                        + (p.getTeamName() != null ? p.getTeamName() : "") + " "
                        + (p.getTeamShortName() != null ? p.getTeamShortName() : "")
                );
                for (String token : tokens) {
                    if (!token.isBlank() && !haystack.contains(token)) {
                        return false;
                    }
                }
            }

            // Teams filter
            if (teams != null && !teams.isEmpty() && !teams.contains(p.getTeamId())) {
                return false;
            }

            // Positions filter
            if (positions != null && !positions.isEmpty() && !positions.contains(p.getElementType())) {
                return false;
            }

            // Price filter (£m converted to tenths)
            if (minPrice != null) {
                int minTenths = (int) Math.round(minPrice * 10);
                if (p.getNowCost() == null || p.getNowCost() < minTenths) {
                    return false;
                }
            }
            if (maxPrice != null) {
                int maxTenths = (int) Math.round(maxPrice * 10);
                if (p.getNowCost() == null || p.getNowCost() > maxTenths) {
                    return false;
                }
            }

            // Status filter
            if (status != null && !status.isEmpty()) {
                List<String> lowerStatus = status.stream().map(String::toLowerCase).toList();
                if (p.getStatus() == null || !lowerStatus.contains(p.getStatus().toLowerCase())) {
                    return false;
                }
            }

            // Ownership filter
            if (minOwnership != null) {
                if (p.getSelectedByPercent() == null || p.getSelectedByPercent().doubleValue() < minOwnership) {
                    return false;
                }
            }
            if (maxOwnership != null) {
                if (p.getSelectedByPercent() == null || p.getSelectedByPercent().doubleValue() > maxOwnership) {
                    return false;
                }
            }

            // In user squad filter
            if (inSquad != null && p.isInSquad() != inSquad) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());

        // 5. Sorting (NULLS ALWAYS LAST regardless of asc/desc; secondary tie-break by player ID asc)
        boolean ascending = actualSortDir.equals("asc");
        Comparator<PlayerSummaryDto> comparator = getComparator(actualSortBy, ascending);
        filtered.sort(comparator);

        // 6. Pagination
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<PlayerSummaryDto> pageContent = filtered.subList(fromIndex, toIndex);

        return PlayerPageResponse.builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .hasPrevious(page > 0 && totalPages > 0)
                .build();
    }

    @Transactional(readOnly = true)
    public PlayerDetailDto getPlayerDetail(Integer id) {
        Player player = playerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        Gameweek currentGw = resolveCurrentGameweek();
        Gameweek nextGw = resolveNextGameweek(currentGw);
        int nextGwId = nextGw != null ? nextGw.getId() : (currentGw != null ? currentGw.getId() : 1);

        Map<Integer, List<Fixture>> nextFixturesByTeam = getFixturesForGameweek(nextGwId);
        Map<Integer, Prediction> nextPredictionsByPlayer = getPredictionsForGameweek(nextGwId);
        Map<Integer, MyTeamPick> userPicksMap = getUserSquadPicks(currentGw != null ? currentGw.getId() : nextGwId);

        PlayerSummaryDto summary = toSummaryDto(player, player.getTeam(), nextGwId, nextFixturesByTeam, nextPredictionsByPlayer, userPicksMap);

        // Fetch upcoming 6 fixtures
        int teamId = player.getTeam() != null ? player.getTeam().getId() : 0;
        int endGwId = nextGwId + 5;
        List<Fixture> fixturesRange = fixtureRepo.findByGameweekIdBetween(nextGwId, endGwId);
        List<Gameweek> gameweeksRange = gameweekRepo.findAll().stream()
                .filter(gw -> gw.getId() >= nextGwId && gw.getId() <= endGwId)
                .sorted(Comparator.comparingInt(Gameweek::getId))
                .toList();
        Map<Integer, Gameweek> gwMap = gameweeksRange.stream().collect(Collectors.toMap(Gameweek::getId, g -> g));

        Map<Integer, List<Fixture>> teamFixturesByGw = fixturesRange.stream()
                .filter(f -> f.getTeamHome() != null && f.getTeamAway() != null &&
                        (f.getTeamHome().getId().equals(teamId) || f.getTeamAway().getId().equals(teamId)))
                .collect(Collectors.groupingBy(f -> f.getGameweek() != null ? f.getGameweek().getId() : nextGwId));

        List<PlayerDetailDto.UpcomingFixtureDto> upcomingFixtures = new ArrayList<>();
        for (int gwId = nextGwId; gwId <= endGwId; gwId++) {
            List<Fixture> gwFixtures = teamFixturesByGw.getOrDefault(gwId, Collections.emptyList());
            Gameweek g = gwMap.get(gwId);
            String gwName = g != null ? g.getName() : "GW" + gwId;

            if (gwFixtures.isEmpty()) {
                upcomingFixtures.add(PlayerDetailDto.UpcomingFixtureDto.builder()
                        .gameweekId(gwId)
                        .gameweekName(gwName)
                        .isBlankGameweek(true)
                        .build());
            } else {
                boolean isDgw = gwFixtures.size() > 1;
                for (Fixture f : gwFixtures) {
                    boolean isHome = f.getTeamHome().getId().equals(teamId);
                    Team opp = isHome ? f.getTeamAway() : f.getTeamHome();
                    Integer diff = isHome ?
                            (f.getTeamHomeDifficulty() != null ? f.getTeamHomeDifficulty().intValue() : 3) :
                            (f.getTeamAwayDifficulty() != null ? f.getTeamAwayDifficulty().intValue() : 3);

                    upcomingFixtures.add(PlayerDetailDto.UpcomingFixtureDto.builder()
                            .fixtureId(f.getId())
                            .gameweekId(gwId)
                            .gameweekName(gwName)
                            .opponentTeamId(opp.getId())
                            .opponentName(opp.getName())
                            .opponentShortName(opp.getShortName())
                            .isHome(isHome)
                            .difficulty(diff)
                            .kickoffTime(f.getKickoffTime())
                            .isDoubleGameweek(isDgw)
                            .isBlankGameweek(false)
                            .build());
                }
            }
        }

        // Fetch predictions for player over next 6 GWs
        List<Prediction> predictions = predictionRepo.findByPlayerIdAndGameweekIdBetween(id, nextGwId, endGwId);
        Map<Integer, Prediction> predByGw = predictions.stream()
                .filter(p -> p.getGameweek() != null)
                .collect(Collectors.toMap(p -> p.getGameweek().getId(), p -> p, (a, b) -> a));

        List<PlayerDetailDto.GameweekPredictionDto> predictionDtos = new ArrayList<>();
        BigDecimal xp3Sum = BigDecimal.ZERO;
        BigDecimal xp6Sum = BigDecimal.ZERO;
        boolean hasAnyXp3 = false;
        boolean hasAnyXp6 = false;

        for (int gwId = nextGwId; gwId <= endGwId; gwId++) {
            Prediction p = predByGw.get(gwId);
            if (p != null && p.getExpectedPoints() != null) {
                predictionDtos.add(PlayerDetailDto.GameweekPredictionDto.builder()
                        .gameweekId(gwId)
                        .expectedPoints(p.getExpectedPoints())
                        .floor(p.getFloor())
                        .ceiling(p.getCeiling())
                        .startProbability(p.getStartProbability())
                        .build());

                xp6Sum = xp6Sum.add(p.getExpectedPoints());
                hasAnyXp6 = true;

                if (gwId <= nextGwId + 2) {
                    xp3Sum = xp3Sum.add(p.getExpectedPoints());
                    hasAnyXp3 = true;
                }
            }
        }

        // Historical Match Stats
        List<PlayerGameweekStats> statsList = playerGameweekStatsRepo.findByPlayerIdOrderByGameweekIdDesc(id);
        List<PlayerDetailDto.HistoricalStatDto> historicalDtos = statsList.stream().map(s -> {
            String oppShort = null;
            Boolean wasHome = null;
            if (s.getFixture() != null && player.getTeam() != null) {
                Fixture f = s.getFixture();
                if (f.getTeamHome() != null && f.getTeamAway() != null) {
                    if (f.getTeamHome().getId().equals(player.getTeam().getId())) {
                        wasHome = true;
                        oppShort = f.getTeamAway().getShortName();
                    } else {
                        wasHome = false;
                        oppShort = f.getTeamHome().getShortName();
                    }
                }
            }

            return PlayerDetailDto.HistoricalStatDto.builder()
                    .gameweekId(s.getGameweek() != null ? s.getGameweek().getId() : null)
                    .opponentShortName(oppShort)
                    .wasHome(wasHome)
                    .minutes(s.getMinutes())
                    .totalPoints(s.getTotalPoints())
                    .goalsScored(s.getGoalsScored())
                    .assists(s.getAssists())
                    .cleanSheets(s.getCleanSheets())
                    .goalsConceded(s.getGoalsConceded())
                    .bonus(s.getBonus())
                    .bps(s.getBps())
                    .expectedGoals(s.getExpectedGoals())
                    .expectedAssists(s.getExpectedAssists())
                    .expectedGoalInvolvements(s.getExpectedGoalInvolvements())
                    .build();
        }).collect(Collectors.toList());

        return PlayerDetailDto.builder()
                .summary(summary)
                .xp3(hasAnyXp3 ? xp3Sum.setScale(2, RoundingMode.HALF_UP) : null)
                .xp6(hasAnyXp6 ? xp6Sum.setScale(2, RoundingMode.HALF_UP) : null)
                .upcomingFixtures(upcomingFixtures)
                .historicalStats(historicalDtos)
                .predictionsByGameweek(predictionDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public PlayerMetaDto getMeta() {
        List<Team> allTeams = teamRepo.findAll();
        List<PlayerMetaDto.TeamOptionDto> teamOptions = allTeams.stream()
                .sorted(Comparator.comparing(Team::getName))
                .map(t -> PlayerMetaDto.TeamOptionDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .shortName(t.getShortName())
                        .build())
                .collect(Collectors.toList());

        List<PlayerMetaDto.PositionOptionDto> positionOptions = List.of(
                PlayerMetaDto.PositionOptionDto.builder().elementType((short) 1).shortName("GKP").singularName("Goalkeeper").build(),
                PlayerMetaDto.PositionOptionDto.builder().elementType((short) 2).shortName("DEF").singularName("Defender").build(),
                PlayerMetaDto.PositionOptionDto.builder().elementType((short) 3).shortName("MID").singularName("Midfielder").build(),
                PlayerMetaDto.PositionOptionDto.builder().elementType((short) 4).shortName("FWD").singularName("Forward").build()
        );

        List<Player> allPlayers = playerRepo.findAll();
        double minPrice = 3.5;
        double maxPrice = 15.0;
        double minOwnership = 0.0;
        double maxOwnership = 100.0;

        if (!allPlayers.isEmpty()) {
            minPrice = allPlayers.stream()
                    .filter(p -> p.getNowCost() != null)
                    .mapToDouble(p -> p.getNowCost() / 10.0)
                    .min().orElse(3.5);
            maxPrice = allPlayers.stream()
                    .filter(p -> p.getNowCost() != null)
                    .mapToDouble(p -> p.getNowCost() / 10.0)
                    .max().orElse(15.0);

            minOwnership = allPlayers.stream()
                    .filter(p -> p.getSelectedByPercent() != null)
                    .mapToDouble(p -> p.getSelectedByPercent().doubleValue())
                    .min().orElse(0.0);
            maxOwnership = allPlayers.stream()
                    .filter(p -> p.getSelectedByPercent() != null)
                    .mapToDouble(p -> p.getSelectedByPercent().doubleValue())
                    .max().orElse(100.0);
        }

        Gameweek currentGw = resolveCurrentGameweek();
        Gameweek nextGw = resolveNextGameweek(currentGw);

        return PlayerMetaDto.builder()
                .teams(teamOptions)
                .positions(positionOptions)
                .priceRange(PlayerMetaDto.MinMaxDto.builder().min(minPrice).max(maxPrice).build())
                .ownershipRange(PlayerMetaDto.MinMaxDto.builder().min(minOwnership).max(maxOwnership).build())
                .totalPlayers(allPlayers.size())
                .currentGameweek(currentGw != null ? currentGw.getId() : null)
                .nextGameweek(nextGw != null ? nextGw.getId() : null)
                .build();
    }

    // ─── Helpers ────────────────────────────────────────────────────────

    private PlayerSummaryDto toSummaryDto(Player p,
                                          Team t,
                                          int nextGwId,
                                          Map<Integer, List<Fixture>> nextFixturesByTeam,
                                          Map<Integer, Prediction> nextPredictionsByPlayer,
                                          Map<Integer, MyTeamPick> userPicksMap) {
        Integer teamId = t != null ? t.getId() : (p.getTeam() != null ? p.getTeam().getId() : null);
        String teamName = t != null ? t.getName() : "Unknown";
        String teamShort = t != null ? t.getShortName() : "???";

        // Squad pick info
        MyTeamPick userPick = userPicksMap.get(p.getId());
        boolean inSquad = userPick != null;
        boolean isStarting = userPick != null && userPick.getPosition() != null && userPick.getPosition() <= 11;
        boolean isCaptain = userPick != null && Boolean.TRUE.equals(userPick.getIsCaptain());
        boolean isViceCaptain = userPick != null && Boolean.TRUE.equals(userPick.getIsViceCaptain());

        // Next fixture info
        List<Fixture> fixtures = teamId != null ? nextFixturesByTeam.getOrDefault(teamId, Collections.emptyList()) : Collections.emptyList();
        String nextOpponentShortName = null;
        Integer nextOpponentDifficulty = null;
        boolean isDoubleGameweek = fixtures.size() > 1;
        boolean isBlankGameweek = fixtures.isEmpty();

        if (isBlankGameweek) {
            nextOpponentShortName = "BLANK";
        } else if (fixtures.size() == 1) {
            Fixture f = fixtures.get(0);
            boolean isHome = f.getTeamHome() != null && f.getTeamHome().getId().equals(teamId);
            Team opp = isHome ? f.getTeamAway() : f.getTeamHome();
            String oppShort = opp != null ? opp.getShortName() : "???";
            nextOpponentShortName = oppShort + (isHome ? " (H)" : " (A)");
            nextOpponentDifficulty = isHome ?
                    (f.getTeamHomeDifficulty() != null ? f.getTeamHomeDifficulty().intValue() : 3) :
                    (f.getTeamAwayDifficulty() != null ? f.getTeamAwayDifficulty().intValue() : 3);
        } else {
            // Double GW: format e.g. "ARS (H), CHE (A)"
            List<String> oppStrings = new ArrayList<>();
            int totalDiff = 0;
            int countDiff = 0;
            for (Fixture f : fixtures) {
                boolean isHome = f.getTeamHome() != null && f.getTeamHome().getId().equals(teamId);
                Team opp = isHome ? f.getTeamAway() : f.getTeamHome();
                String oppShort = opp != null ? opp.getShortName() : "???";
                oppStrings.add(oppShort + (isHome ? " (H)" : " (A)"));
                Integer diff = isHome ?
                        (f.getTeamHomeDifficulty() != null ? f.getTeamHomeDifficulty().intValue() : null) :
                        (f.getTeamAwayDifficulty() != null ? f.getTeamAwayDifficulty().intValue() : null);
                if (diff != null) {
                    totalDiff += diff;
                    countDiff++;
                }
            }
            nextOpponentShortName = String.join(", ", oppStrings);
            nextOpponentDifficulty = countDiff > 0 ? (int) Math.round((double) totalDiff / countDiff) : 3;
        }

        // Next prediction xPoints
        Prediction pred = nextPredictionsByPlayer.get(p.getId());
        BigDecimal xpNext = (pred != null && pred.getExpectedPoints() != null) ? pred.getExpectedPoints() : null;

        return PlayerSummaryDto.builder()
                .id(p.getId())
                .webName(p.getWebName())
                .firstName(p.getFirstName())
                .secondName(p.getSecondName())
                .teamId(teamId)
                .teamName(teamName)
                .teamShortName(teamShort)
                .elementType(p.getElementType())
                .positionName(mapPosition(p.getElementType()))
                .nowCost(p.getNowCost())
                .costChangeStart(p.getCostChangeStart() != null ? p.getCostChangeStart() : 0)
                .costChangeEvent(p.getCostChangeEvent() != null ? p.getCostChangeEvent() : 0)
                .status(p.getStatus() != null ? p.getStatus() : "a")
                .chanceOfPlayingNext(p.getChanceOfPlayingNext())
                .chanceOfPlayingThisRound(p.getChanceOfPlayingThisRound())
                .news(p.getNews())
                .newsAdded(p.getNewsAdded())
                .form(p.getForm())
                .totalPoints(p.getTotalPoints() != null ? p.getTotalPoints() : 0)
                .pointsPerGame(p.getPointsPerGame())
                .selectedByPercent(p.getSelectedByPercent())
                .minutes(p.getMinutes() != null ? p.getMinutes() : 0)
                .starts(p.getStarts() != null ? p.getStarts() : 0)
                .goalsScored(p.getGoalsScored() != null ? p.getGoalsScored() : 0)
                .assists(p.getAssists() != null ? p.getAssists() : 0)
                .cleanSheets(p.getCleanSheets() != null ? p.getCleanSheets() : 0)
                .goalsConceded(p.getGoalsConceded() != null ? p.getGoalsConceded() : 0)
                .ownGoals(p.getOwnGoals() != null ? p.getOwnGoals() : 0)
                .penaltiesSaved(p.getPenaltiesSaved() != null ? p.getPenaltiesSaved() : 0)
                .penaltiesMissed(p.getPenaltiesMissed() != null ? p.getPenaltiesMissed() : 0)
                .yellowCards(p.getYellowCards() != null ? p.getYellowCards() : 0)
                .redCards(p.getRedCards() != null ? p.getRedCards() : 0)
                .saves(p.getSaves() != null ? p.getSaves() : 0)
                .bonus(p.getBonus() != null ? p.getBonus() : 0)
                .bps(p.getBps() != null ? p.getBps() : 0)
                .influence(p.getInfluence())
                .creativity(p.getCreativity())
                .threat(p.getThreat())
                .ictIndex(p.getIctIndex())
                .expectedGoals(p.getExpectedGoals())
                .expectedAssists(p.getExpectedAssists())
                .expectedGoalInvolvements(p.getExpectedGoalInvolvements())
                .expectedGoalsConceded(p.getExpectedGoalsConceded())
                .transfersInEvent(p.getTransfersInEvent() != null ? p.getTransfersInEvent() : 0)
                .transfersOutEvent(p.getTransfersOutEvent() != null ? p.getTransfersOutEvent() : 0)
                .epNext(p.getEpNext())
                .xpNext(xpNext)
                .inSquad(inSquad)
                .isStarting(isStarting)
                .isCaptain(isCaptain)
                .isViceCaptain(isViceCaptain)
                .nextOpponentShortName(nextOpponentShortName)
                .nextOpponentDifficulty(nextOpponentDifficulty)
                .isDoubleGameweek(isDoubleGameweek)
                .isBlankGameweek(isBlankGameweek)
                .build();
    }

    private String mapPosition(Short type) {
        if (type == null) return "MID";
        return switch (type.intValue()) {
            case 1 -> "GKP";
            case 2 -> "DEF";
            case 3 -> "MID";
            case 4 -> "FWD";
            default -> "MID";
        };
    }

    private Comparator<PlayerSummaryDto> getComparator(String sortBy, boolean ascending) {
        return switch (sortBy.toLowerCase()) {
            case "total_points", "totalpoints" -> buildComparator(PlayerSummaryDto::getTotalPoints, ascending);
            case "expected_points", "expectedpoints", "xp_next", "xpnext" -> buildComparator(PlayerSummaryDto::getXpNext, ascending);
            case "form" -> buildComparator(PlayerSummaryDto::getForm, ascending);
            case "now_cost", "nowcost", "price" -> buildComparator(PlayerSummaryDto::getNowCost, ascending);
            case "selected_by_percent", "selectedbypercent", "ownership" -> buildComparator(PlayerSummaryDto::getSelectedByPercent, ascending);
            case "goals_scored", "goalsscored" -> buildComparator(PlayerSummaryDto::getGoalsScored, ascending);
            case "assists" -> buildComparator(PlayerSummaryDto::getAssists, ascending);
            case "clean_sheets", "cleansheets" -> buildComparator(PlayerSummaryDto::getCleanSheets, ascending);
            case "minutes" -> buildComparator(PlayerSummaryDto::getMinutes, ascending);
            case "starts" -> buildComparator(PlayerSummaryDto::getStarts, ascending);
            case "ict_index", "ictindex" -> buildComparator(PlayerSummaryDto::getIctIndex, ascending);
            case "expected_goals", "expectedgoals", "xg" -> buildComparator(PlayerSummaryDto::getExpectedGoals, ascending);
            case "expected_assists", "expectedassists", "xa" -> buildComparator(PlayerSummaryDto::getExpectedAssists, ascending);
            case "expected_goal_involvements", "expectedgoalinvolvements", "xgi" -> buildComparator(PlayerSummaryDto::getExpectedGoalInvolvements, ascending);
            case "expected_goals_conceded", "expectedgoalsconceded", "xgc" -> buildComparator(PlayerSummaryDto::getExpectedGoalsConceded, ascending);
            case "transfers_in_event", "transfersinevent" -> buildComparator(PlayerSummaryDto::getTransfersInEvent, ascending);
            case "transfers_out_event", "transfersoutevent" -> buildComparator(PlayerSummaryDto::getTransfersOutEvent, ascending);
            case "cost_change_start", "costchangestart" -> buildComparator(PlayerSummaryDto::getCostChangeStart, ascending);
            case "cost_change_event", "costchangeevent" -> buildComparator(PlayerSummaryDto::getCostChangeEvent, ascending);
            case "bonus" -> buildComparator(PlayerSummaryDto::getBonus, ascending);
            case "bps" -> buildComparator(PlayerSummaryDto::getBps, ascending);
            case "points_per_game", "pointspergame" -> buildComparator(PlayerSummaryDto::getPointsPerGame, ascending);
            case "ep_next", "epnext" -> buildComparator(PlayerSummaryDto::getEpNext, ascending);
            case "web_name", "webname", "name" -> buildComparator(PlayerSummaryDto::getWebName, ascending);
            default -> throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        };
    }

    private <T extends Comparable<T>> Comparator<PlayerSummaryDto> buildComparator(
            Function<PlayerSummaryDto, T> extractor, boolean ascending) {
        return (a, b) -> {
            T valA = extractor.apply(a);
            T valB = extractor.apply(b);
            if (valA == null && valB == null) {
                return a.getId().compareTo(b.getId());
            }
            if (valA == null) return 1; // nulls always last
            if (valB == null) return -1; // nulls always last
            int cmp = valA.compareTo(valB);
            if (cmp != 0) {
                return ascending ? cmp : -cmp;
            }
            return a.getId().compareTo(b.getId());
        };
    }

    private Gameweek resolveCurrentGameweek() {
        return gameweekRepo.findByIsCurrentTrue()
                .or(() -> gameweekRepo.findByIsNextTrue())
                .or(() -> gameweekRepo.findById(1))
                .or(() -> gameweekRepo.findAll().stream().findFirst())
                .orElse(null);
    }

    private Gameweek resolveNextGameweek(Gameweek currentGw) {
        return gameweekRepo.findByIsNextTrue()
                .or(() -> currentGw != null ? Optional.of(currentGw) : Optional.empty())
                .orElse(null);
    }

    private Map<Integer, List<Fixture>> getFixturesForGameweek(int gwId) {
        List<Fixture> fixtures = fixtureRepo.findByGameweekId(gwId);
        Map<Integer, List<Fixture>> map = new HashMap<>();
        for (Fixture f : fixtures) {
            if (f.getTeamHome() != null) {
                map.computeIfAbsent(f.getTeamHome().getId(), k -> new ArrayList<>()).add(f);
            }
            if (f.getTeamAway() != null) {
                map.computeIfAbsent(f.getTeamAway().getId(), k -> new ArrayList<>()).add(f);
            }
        }
        return map;
    }

    private Map<Integer, Prediction> getPredictionsForGameweek(int gwId) {
        List<Prediction> predictions = predictionRepo.findByGameweekId(gwId);
        Map<Integer, Prediction> map = new HashMap<>();
        for (Prediction p : predictions) {
            // Keep first or latest
            map.putIfAbsent(p.getPlayer().getId(), p);
        }
        return map;
    }

    private Map<Integer, MyTeamPick> getUserSquadPicks(int gwId) {
        MyTeamState state = myTeamStateRepo.findByGameweekId(gwId)
                .or(() -> myTeamStateRepo.findTopByOrderByGameweekIdDesc())
                .orElse(null);
        if (state == null) {
            return Collections.emptyMap();
        }
        List<MyTeamPick> picks = myTeamPickRepo.findByGameweekIdOrderByPositionAsc(state.getGameweek().getId());
        return picks.stream()
                .filter(p -> p.getPlayer() != null)
                .collect(Collectors.toMap(p -> p.getPlayer().getId(), p -> p, (a, b) -> a));
    }
}
