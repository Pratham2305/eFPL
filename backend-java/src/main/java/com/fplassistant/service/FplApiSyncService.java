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
    private final MyTeamAutoSubRepository myTeamAutoSubRepo;
    private final PlayerGameweekStatsRepository playerGameweekStatsRepo;
    private final TransferLogRepository transferLogRepo;
    private final RestTemplate restTemplate;

    public FplApiSyncService(FplApiClient fplApiClient, AppProperties appProperties,
                             TeamRepository teamRepo, PlayerRepository playerRepo,
                             GameweekRepository gameweekRepo, FixtureRepository fixtureRepo,
                             MyTeamPickRepository myTeamPickRepo, MyTeamStateRepository myTeamStateRepo,
                             MyTeamAutoSubRepository myTeamAutoSubRepo,
                             PlayerGameweekStatsRepository playerGameweekStatsRepo,
                             TransferLogRepository transferLogRepo,
                             RestTemplate restTemplate) {
        this.fplApiClient = fplApiClient;
        this.appProperties = appProperties;
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.gameweekRepo = gameweekRepo;
        this.fixtureRepo = fixtureRepo;
        this.myTeamPickRepo = myTeamPickRepo;
        this.myTeamStateRepo = myTeamStateRepo;
        this.myTeamAutoSubRepo = myTeamAutoSubRepo;
        this.playerGameweekStatsRepo = playerGameweekStatsRepo;
        this.transferLogRepo = transferLogRepo;
        this.restTemplate = restTemplate;
    }

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_DATE_TIME;

    @Transactional
    public SyncResponse fullSync() {
        return fullSync(null, false);
    }

    @Transactional
    public SyncResponse fullSync(Integer overrideTeamId) {
        return fullSync(overrideTeamId, false);
    }

    /**
     * Full sync: bootstrap-static (teams, players, gameweeks) + fixtures + user's squad & history.
     */
    @Transactional
    public SyncResponse fullSync(Integer overrideTeamId, boolean forceHistory) {
        log.info("Starting full FPL data sync (forceHistory={})…", forceHistory);

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

        // 3. User's squad & history
        int targetTeamId = overrideTeamId != null && overrideTeamId > 0 ? overrideTeamId : appProperties.getFpl().getTeamId();
        if (targetTeamId <= 0) {
            targetTeamId = 1; // Default sample FPL team ID
        }

        int historyUpdated = 0;
        int picksUpdated = 0;
        int statsUpdated = 0;
        int transfersUpdated = 0;

        try {
            int[] counts = syncUserTeamAndHistory(targetTeamId, forceHistory);
            historyUpdated = counts[0];
            picksUpdated = counts[1];
            statsUpdated = counts[2];
            transfersUpdated = counts[3];
            log.info("Successfully synced squad/history for FPL Team ID {}: {} history GWs, {} picks, {} stats, {} transfers",
                    targetTeamId, historyUpdated, picksUpdated, statsUpdated, transfersUpdated);
            notifyMlService(targetTeamId);
        } catch (Exception e) {
            log.warn("Failed to sync user team (id={}): {}", targetTeamId, e.getMessage(), e);
        }

        log.info("Full sync complete: {} teams, {} gameweeks, {} players, {} fixtures",
                teamsUpdated, gameweeksUpdated, playersUpdated, fixturesUpdated);

        return SyncResponse.builder()
                .status("OK")
                .message("Full sync completed successfully")
                .playersUpdated(playersUpdated)
                .fixturesUpdated(fixturesUpdated)
                .gameweeksUpdated(gameweeksUpdated)
                .historyGameweeksUpdated(historyUpdated)
                .picksUpdated(picksUpdated)
                .statsUpdated(statsUpdated)
                .transfersUpdated(transfersUpdated)
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

    // ─── User Team & History ─────────────────────────────────

    private int[] syncUserTeamAndHistory(int teamId, boolean forceHistory) {
        int historyCount = 0;
        int picksCount = 0;
        int statsCount = 0;
        int transfersCount = 0;

        FplEntryResponse entry = fplApiClient.fetchEntry(teamId);
        int currentGw = (entry != null && entry.getCurrentEvent() != null) ? entry.getCurrentEvent() : 1;

        // 1. Fetch Entry History
        FplHistoryResponse history = fplApiClient.fetchEntryHistory(teamId);
        if (history != null && history.getCurrent() != null) {
            for (FplHistoryResponse.HistoryCurrent hc : history.getCurrent()) {
                if (hc.getEvent() == null) continue;
                Gameweek gw = gameweekRepo.findById(hc.getEvent()).orElse(null);
                if (gw == null) continue;

                MyTeamState state = myTeamStateRepo.findByGameweekId(gw.getId())
                        .orElse(new MyTeamState());
                state.setGameweek(gw);
                state.setGameweekPoints(hc.getPoints());
                int cost = hc.getEventTransfersCost() != null ? hc.getEventTransfersCost() : 0;
                state.setGameweekNetPoints(hc.getPoints() != null ? hc.getPoints() - cost : null);
                state.setOverallPoints(hc.getTotalPoints());
                state.setOverallRank(hc.getOverallRank());
                state.setRank(hc.getRank());
                state.setRankSort(hc.getRankSort());
                state.setBank(hc.getBank());
                state.setTeamValue(hc.getValue());
                state.setEventTransfers(hc.getEventTransfers());
                state.setEventTransfersCost(hc.getEventTransfersCost());
                state.setPointsOnBench(hc.getPointsOnBench());

                // Check chips used
                if (history.getChips() != null) {
                    for (FplHistoryResponse.HistoryChip chip : history.getChips()) {
                        if (gw.getId().equals(chip.getEvent())) {
                            state.setActiveChip(chip.getName());
                            if ("3xc".equalsIgnoreCase(chip.getName())) {
                                state.setTripleCaptainActive(true);
                                state.setTripleCaptainUsed(true);
                            } else if ("bboost".equalsIgnoreCase(chip.getName())) {
                                state.setBenchBoostActive(true);
                                state.setBenchBoostUsed(true);
                            } else if ("freehit".equalsIgnoreCase(chip.getName())) {
                                state.setFreeHitActive(true);
                                state.setFreeHitUsed(true);
                            } else if ("wildcard".equalsIgnoreCase(chip.getName())) {
                                state.setWildcardActive(true);
                                state.setWildcardUsed(true);
                            }
                        }
                    }
                }

                myTeamStateRepo.save(state);
                historyCount++;
            }
        }

        // 2. Fetch Transfers
        try {
            List<FplTransferResponse> transfers = fplApiClient.fetchTransfers(teamId);
            if (transfers != null) {
                for (FplTransferResponse t : transfers) {
                    if (t.getEvent() == null || t.getElementIn() == null || t.getElementOut() == null) continue;
                    Gameweek gw = gameweekRepo.findById(t.getEvent()).orElse(null);
                    Player pIn = playerRepo.findById(t.getElementIn()).orElse(null);
                    Player pOut = playerRepo.findById(t.getElementOut()).orElse(null);
                    LocalDateTime tTime = parseDateTime(t.getTime());

                    if (gw != null && pIn != null && pOut != null) {
                        boolean exists = transferLogRepo.existsByGameweekIdAndPlayerInIdAndPlayerOutIdAndTransferTime(
                                gw.getId(), pIn.getId(), pOut.getId(), tTime);
                        if (!exists) {
                            TransferLog tl = TransferLog.builder()
                                    .gameweek(gw)
                                    .playerIn(pIn)
                                    .playerOut(pOut)
                                    .playerInCost(t.getElementInCost())
                                    .playerOutCost(t.getElementOutCost())
                                    .transferTime(tTime)
                                    .build();
                            transferLogRepo.save(tl);
                            transfersCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to sync transfers for team {}: {}", teamId, e.getMessage());
        }

        // 3. For all gameweeks up to currentGw: sync picks & live player stats
        int maxGwToSync = Math.max(currentGw, 1);
        for (int gwId = 1; gwId <= maxGwToSync; gwId++) {
            Gameweek gw = gameweekRepo.findById(gwId).orElse(null);
            if (gw == null) continue;

            boolean isFinished = Boolean.TRUE.equals(gw.getFinished());
            boolean isDataChecked = Boolean.TRUE.equals(gw.getDataChecked());

            // A. Picks & AutoSubs
            long existingPicks = myTeamPickRepo.countByGameweekId(gwId);
            if (existingPicks == 15 && isFinished && !forceHistory) {
                log.debug("Skipping picks for settled GW {}", gwId);
            } else {
                try {
                    FplPicksResponse picksResp = fplApiClient.fetchPicks(teamId, gwId);
                    if (picksResp != null && picksResp.getPicks() != null && !picksResp.getPicks().isEmpty()) {
                        myTeamPickRepo.deleteByGameweekId(gwId);
                        myTeamAutoSubRepo.deleteByGameweekId(gwId);

                        for (FplPicksResponse.FplPick p : picksResp.getPicks()) {
                            Player player = playerRepo.findById(p.getElement()).orElse(null);
                            if (player == null) continue;

                            MyTeamPick pick = MyTeamPick.builder()
                                    .gameweek(gw)
                                    .player(player)
                                    .position(p.getPosition().shortValue())
                                    .multiplier(p.getMultiplier().shortValue())
                                    .isCaptain(Boolean.TRUE.equals(p.getIsCaptain()))
                                    .isViceCaptain(Boolean.TRUE.equals(p.getIsViceCaptain()))
                                    .build();
                            myTeamPickRepo.save(pick);
                            picksCount++;
                        }

                        if (picksResp.getAutomaticSubs() != null) {
                            for (FplPicksResponse.FplAutoSub sub : picksResp.getAutomaticSubs()) {
                                Player in = playerRepo.findById(sub.getElementIn()).orElse(null);
                                Player out = playerRepo.findById(sub.getElementOut()).orElse(null);
                                if (in != null && out != null) {
                                    MyTeamAutoSub autoSub = MyTeamAutoSub.builder()
                                            .gameweek(gw)
                                            .playerIn(in)
                                            .playerOut(out)
                                            .build();
                                    myTeamAutoSubRepo.save(autoSub);
                                }
                            }
                        }

                        if (picksResp.getActiveChip() != null) {
                            MyTeamState st = myTeamStateRepo.findByGameweekId(gwId).orElse(null);
                            if (st != null) {
                                st.setActiveChip(picksResp.getActiveChip());
                                myTeamStateRepo.save(st);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch picks for GW {}: {}", gwId, e.getMessage());
                }
            }

            // B. Live Player Gameweek Stats
            long existingStats = playerGameweekStatsRepo.countByGameweekId(gwId);
            if (existingStats > 0 && isFinished && isDataChecked && !forceHistory) {
                log.debug("Skipping live stats for settled and checked GW {}", gwId);
            } else {
                try {
                    Thread.sleep(150); // Be polite to the FPL API
                } catch (InterruptedException ignored) {}

                try {
                    FplLiveResponse liveResp = fplApiClient.fetchEventLive(gwId);
                    if (liveResp != null && liveResp.getElements() != null) {
                        for (FplLiveResponse.LiveElement le : liveResp.getElements()) {
                            Player player = playerRepo.findById(le.getId()).orElse(null);
                            if (player == null) continue;

                            PlayerGameweekStats pgs = playerGameweekStatsRepo
                                    .findFirstByPlayerIdAndGameweekId(player.getId(), gwId)
                                    .orElse(new PlayerGameweekStats());

                            pgs.setPlayer(player);
                            pgs.setGameweek(gw);

                            FplLiveResponse.LiveStats s = le.getStats();
                            if (s != null) {
                                pgs.setTotalPoints(s.getTotalPoints());
                                pgs.setMinutes(s.getMinutes());
                                pgs.setStarts(s.getStarts() != null ? s.getStarts() : 0);
                                pgs.setGoalsScored(s.getGoalsScored());
                                pgs.setAssists(s.getAssists());
                                pgs.setCleanSheets(s.getCleanSheets());
                                pgs.setGoalsConceded(s.getGoalsConceded());
                                pgs.setOwnGoals(s.getOwnGoals());
                                pgs.setPenaltiesSaved(s.getPenaltiesSaved());
                                pgs.setPenaltiesMissed(s.getPenaltiesMissed());
                                pgs.setYellowCards(s.getYellowCards());
                                pgs.setRedCards(s.getRedCards());
                                pgs.setSaves(s.getSaves());
                                pgs.setBonus(s.getBonus());
                                pgs.setBps(s.getBps());
                                pgs.setInfluence(s.getInfluence());
                                pgs.setCreativity(s.getCreativity());
                                pgs.setThreat(s.getThreat());
                                pgs.setIctIndex(s.getIctIndex());
                                pgs.setExpectedGoals(s.getExpectedGoals());
                                pgs.setExpectedAssists(s.getExpectedAssists());
                                pgs.setExpectedGoalInvolvements(s.getExpectedGoalInvolvements());
                                pgs.setExpectedGoalsConceded(s.getExpectedGoalsConceded());
                                pgs.setInDreamteam(Boolean.TRUE.equals(s.getInDreamteam()));
                                pgs.setDefensiveContribution(s.getDefensiveContribution() != null ? s.getDefensiveContribution() : 0);
                                pgs.setRecoveries(s.getRecoveries() != null ? s.getRecoveries() : 0);
                                pgs.setTackles(s.getTackles() != null ? s.getTackles() : 0);
                                pgs.setClearancesBlocksInterceptions(s.getClearancesBlocksInterceptions() != null ? s.getClearancesBlocksInterceptions() : 0);
                            }
                            playerGameweekStatsRepo.save(pgs);
                            statsCount++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch live stats for GW {}: {}", gwId, e.getMessage());
                }
            }
        }

        return new int[]{historyCount, picksCount, statsCount, transfersCount};
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
