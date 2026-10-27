package com.fplassistant.service;

import com.fplassistant.client.*;
import com.fplassistant.config.AppProperties;
import com.fplassistant.dto.SyncResponse;
import com.fplassistant.exception.FplApiException;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Pulls data from the official FPL API and persists it into Postgres.
 */
@Service
public class FplApiSyncService {

    private static final Logger log = LoggerFactory.getLogger(FplApiSyncService.class);

    private final FplApiClient fplApiClient;
    private final AppProperties appProperties;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final GameweekRepository gameweekRepo;
    private final FixtureRepository fixtureRepo;
    private final MyTeamPickRepository myTeamPickRepo;
    private final MyTeamStateRepository myTeamStateRepo;
    private final RestTemplate restTemplate;

    public FplApiSyncService(FplApiClient fplApiClient, AppProperties appProperties,
                             TeamRepository teamRepo, PlayerRepository playerRepo,
                             GameweekRepository gameweekRepo, FixtureRepository fixtureRepo,
                             MyTeamPickRepository myTeamPickRepo, MyTeamStateRepository myTeamStateRepo,
                             RestTemplate restTemplate) {
        this.fplApiClient = fplApiClient;
        this.appProperties = appProperties;
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.gameweekRepo = gameweekRepo;
        this.fixtureRepo = fixtureRepo;
        this.myTeamPickRepo = myTeamPickRepo;
        this.myTeamStateRepo = myTeamStateRepo;
        this.restTemplate = restTemplate;
    }

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Full sync: bootstrap-static (teams, players, gameweeks) + fixtures + user's squad.
     */
    @Transactional
    public SyncResponse fullSync() {
        return fullSync(null);
    }

    @Transactional
    public SyncResponse fullSync(Integer overrideTeamId) {
        log.info("Starting full FPL data sync…");

        // 1. Bootstrap static — teams, players, gameweeks
        FplBootstrapResponse bootstrap;
        try {
            bootstrap = fplApiClient.fetchBootstrapStatic();
        } catch (Exception e) {
            throw new FplApiException("Failed to fetch bootstrap-static", e);
        }

        int teamsUpdated = syncTeams(bootstrap.getTeams());
        int gameweeksUpdated = syncGameweeks(bootstrap.getEvents());
        int playersUpdated = syncPlayers(bootstrap.getElements());

        // 2. Fixtures
        int fixturesUpdated = 0;
        try {
            List<FplFixtureResponse> fixtures = fplApiClient.fetchFixtures();
            fixturesUpdated = syncFixtures(fixtures);
        } catch (Exception e) {
            log.warn("Failed to fetch fixtures, continuing: {}", e.getMessage());
        }

        // 3. User's squad (use overrideTeamId, config teamId, or default sample team 1)
        int targetTeamId = overrideTeamId != null && overrideTeamId > 0 ? overrideTeamId : appProperties.getFpl().getTeamId();
        if (targetTeamId <= 0) {
            targetTeamId = 1; // Default sample FPL team ID
        }

        try {
            syncUserTeam(targetTeamId);
            log.info("Successfully synced squad for FPL Team ID {}", targetTeamId);
            notifyMlService(targetTeamId);
        } catch (Exception e) {
            log.warn("Failed to sync user team (id={}): {}", targetTeamId, e.getMessage());
        }

        log.info("Full sync complete: {} teams, {} gameweeks, {} players, {} fixtures",
                teamsUpdated, gameweeksUpdated, playersUpdated, fixturesUpdated);

        return SyncResponse.builder()
                .status("OK")
                .message("Full sync completed successfully")
                .playersUpdated(playersUpdated)
                .fixturesUpdated(fixturesUpdated)
                .gameweeksUpdated(gameweeksUpdated)
                .build();
    }

    private void notifyMlService(int teamId) {
        try {
            String baseUrl = appProperties.getMlService().getBaseUrl();
            log.info("Notifying ML service to sync squad and generate predictions for team {}...", teamId);
            restTemplate.postForObject(baseUrl + "/api/v1/ingest/team?team_id=" + teamId, null, Map.class);
            log.info("ML service team ingestion and prediction pipeline completed.");
        } catch (Exception e) {
            log.warn("Could not notify ML service (is ml-service running?): {}", e.getMessage());
        }
    }

    // ─── Teams ───────────────────────────────────────────────

    private int syncTeams(List<FplBootstrapResponse.FplTeam> fplTeams) {
        if (fplTeams == null) return 0;
        for (FplBootstrapResponse.FplTeam ft : fplTeams) {
            Team team = teamRepo.findById(ft.getId())
                    .orElse(new Team());
            team.setId(ft.getId());
            team.setName(ft.getName());
            team.setShortName(ft.getShortName());
            team.setCode(ft.getCode());
            team.setStrength(ft.getStrength());
            team.setStrengthOverallHome(ft.getStrengthOverallHome());
            team.setStrengthOverallAway(ft.getStrengthOverallAway());
            team.setStrengthAttackHome(ft.getStrengthAttackHome());
            team.setStrengthAttackAway(ft.getStrengthAttackAway());
            team.setStrengthDefenceHome(ft.getStrengthDefenceHome());
            team.setStrengthDefenceAway(ft.getStrengthDefenceAway());
            teamRepo.save(team);
        }
        return fplTeams.size();
    }

    // ─── Gameweeks ───────────────────────────────────────────

    private int syncGameweeks(List<FplBootstrapResponse.FplEvent> events) {
        if (events == null) return 0;
        for (FplBootstrapResponse.FplEvent e : events) {
            Gameweek gw = gameweekRepo.findById(e.getId())
                    .orElse(new Gameweek());
            gw.setId(e.getId());
            gw.setName(e.getName());
            gw.setDeadlineTime(parseDateTime(e.getDeadlineTime()));
            gw.setAverageEntryScore(e.getAverageEntryScore());
            gw.setHighestScore(e.getHighestScore());
            gw.setIsCurrent(e.getIsCurrent());
            gw.setIsNext(e.getIsNext());
            gw.setIsPrevious(e.getIsPrevious());
            gw.setFinished(e.getFinished());
            gw.setDataChecked(e.getDataChecked());
            gameweekRepo.save(gw);
        }
        return events.size();
    }

    // ─── Players ─────────────────────────────────────────────

    private int syncPlayers(List<FplBootstrapResponse.FplElement> elements) {
        if (elements == null) return 0;
        for (FplBootstrapResponse.FplElement el : elements) {
            Team team = teamRepo.findById(el.getTeam()).orElse(null);
            if (team == null) {
                log.warn("Skipping player {} — team {} not found", el.getId(), el.getTeam());
                continue;
            }

            Player player = playerRepo.findById(el.getId())
                    .orElse(new Player());
            player.setId(el.getId());
            player.setTeam(team);
            player.setFirstName(el.getFirstName());
            player.setSecondName(el.getSecondName());
            player.setWebName(el.getWebName());
            player.setElementType(el.getElementType().shortValue());
            player.setNowCost(el.getNowCost());
            player.setStatus(el.getStatus());
            player.setChanceOfPlayingNext(el.getChanceOfPlayingNextRound());
            player.setChanceOfPlayingThisRound(el.getChanceOfPlayingThisRound());
            player.setNews(el.getNews());
            player.setNewsAdded(parseDateTime(el.getNewsAdded()));
            player.setPhoto(el.getPhoto());
            player.setForm(parseBigDecimal(el.getForm()));
            player.setTotalPoints(el.getTotalPoints());
            player.setPointsPerGame(parseBigDecimal(el.getPointsPerGame()));
            player.setSelectedByPercent(parseBigDecimal(el.getSelectedByPercent()));
            player.setMinutes(el.getMinutes());
            player.setStarts(el.getStarts() != null ? el.getStarts() : 0);
            player.setGoalsScored(el.getGoalsScored());
            player.setAssists(el.getAssists());
            player.setCleanSheets(el.getCleanSheets());
            player.setGoalsConceded(el.getGoalsConceded());
            player.setOwnGoals(el.getOwnGoals());
            player.setPenaltiesSaved(el.getPenaltiesSaved());
            player.setPenaltiesMissed(el.getPenaltiesMissed());
            player.setYellowCards(el.getYellowCards());
            player.setRedCards(el.getRedCards());
            player.setSaves(el.getSaves());
            player.setBonus(el.getBonus());
            player.setBps(el.getBps());
            player.setInfluence(parseBigDecimal(el.getInfluence()));
            player.setCreativity(parseBigDecimal(el.getCreativity()));
            player.setThreat(parseBigDecimal(el.getThreat()));
            player.setIctIndex(parseBigDecimal(el.getIctIndex()));
            player.setExpectedGoals(parseBigDecimal(el.getExpectedGoals()));
            player.setExpectedAssists(parseBigDecimal(el.getExpectedAssists()));
            player.setExpectedGoalInvolvements(parseBigDecimal(el.getExpectedGoalInvolvements()));
            player.setExpectedGoalsConceded(parseBigDecimal(el.getExpectedGoalsConceded()));
            player.setTransfersInEvent(el.getTransfersInEvent());
            player.setTransfersOutEvent(el.getTransfersOutEvent());
            player.setCostChangeStart(el.getCostChangeStart() != null ? el.getCostChangeStart() : 0);
            player.setCostChangeEvent(el.getCostChangeEvent() != null ? el.getCostChangeEvent() : 0);
            player.setEpNext(parseBigDecimal(el.getEpNext()));
            playerRepo.save(player);
        }
        return elements.size();
    }

    // ─── Fixtures ────────────────────────────────────────────

    private int syncFixtures(List<FplFixtureResponse> fplFixtures) {
        if (fplFixtures == null) return 0;
        for (FplFixtureResponse ff : fplFixtures) {
            Gameweek gw = ff.getEvent() != null ? gameweekRepo.findById(ff.getEvent()).orElse(null) : null;
            Team home = teamRepo.findById(ff.getTeamH()).orElse(null);
            Team away = teamRepo.findById(ff.getTeamA()).orElse(null);

            if (home == null || away == null) {
                log.warn("Skipping fixture {} — missing team(s)", ff.getId());
                continue;
            }

            Fixture fixture = fixtureRepo.findById(ff.getId())
                    .orElse(new Fixture());
            fixture.setId(ff.getId());
            fixture.setGameweek(gw);
            fixture.setTeamHome(home);
            fixture.setTeamAway(away);
            fixture.setTeamHomeDifficulty(ff.getTeamHDifficulty() != null ? ff.getTeamHDifficulty().shortValue() : null);
            fixture.setTeamAwayDifficulty(ff.getTeamADifficulty() != null ? ff.getTeamADifficulty().shortValue() : null);
            fixture.setKickoffTime(parseDateTime(ff.getKickoffTime()));
            fixture.setFinished(ff.getFinished());
            fixture.setTeamHomeScore(ff.getTeamHScore());
            fixture.setTeamAwayScore(ff.getTeamAScore());
            fixtureRepo.save(fixture);
        }
        return fplFixtures.size();
    }

    // ─── User Team ───────────────────────────────────────────

    private void syncUserTeam(int teamId) {
        FplEntryResponse entry = fplApiClient.fetchEntry(teamId);
        int currentGw = (entry != null && entry.getCurrentEvent() != null) ? entry.getCurrentEvent() : 1;

        Gameweek gw = gameweekRepo.findById(currentGw).orElse(null);
        if (gw == null) {
            gw = gameweekRepo.findById(1).orElse(null);
        }
        if (gw == null) {
            log.warn("Gameweek {} not found in database", currentGw);
            return;
        }

        currentGw = gw.getId();

        // Upsert team state
        MyTeamState state = myTeamStateRepo.findByGameweekId(currentGw)
                .orElse(new MyTeamState());
        state.setGameweek(gw);
        state.setOverallRank(entry.getSummaryOverallRank());
        state.setOverallPoints(entry.getSummaryOverallPoints());
        state.setGameweekPoints(entry.getSummaryEventPoints());
        state.setBank(entry.getLastDeadlineBank());
        state.setTeamValue(entry.getLastDeadlineValue());
        myTeamStateRepo.save(state);

        // Fetch picks for current GW
        try {
            FplPicksResponse picks = fplApiClient.fetchPicks(teamId, currentGw);
            if (picks != null && picks.getPicks() != null) {
                // Clear existing picks for this GW and re-insert
                myTeamPickRepo.deleteByGameweekId(currentGw);
                for (FplPicksResponse.FplPick pick : picks.getPicks()) {
                    Player player = playerRepo.findById(pick.getElement()).orElse(null);
                    if (player == null) continue;

                    MyTeamPick tp = MyTeamPick.builder()
                            .gameweek(gw)
                            .player(player)
                            .position(pick.getPosition().shortValue())
                            .multiplier(pick.getMultiplier().shortValue())
                            .isCaptain(pick.getIsCaptain())
                            .isViceCaptain(pick.getIsViceCaptain())
                            .build();
                    myTeamPickRepo.save(tp);
                }
                log.info("Synced {} picks for GW {}", picks.getPicks().size(), currentGw);
            }
        } catch (Exception e) {
            log.warn("Could not fetch picks for GW {}: {}", currentGw, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────

    private LocalDateTime parseDateTime(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return LocalDateTime.parse(iso.replace("Z", ""), ISO_FMT);
        } catch (Exception e) {
            log.debug("Could not parse datetime: {}", iso);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
