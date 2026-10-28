package com.fplassistant.service;

import com.fplassistant.dto.TeamCurrentResponse;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameweekSquadServiceTest {

    @Mock
    private MyTeamStateRepository myTeamStateRepo;
    @Mock
    private MyTeamPickRepository myTeamPickRepo;
    @Mock
    private MyTeamAutoSubRepository myTeamAutoSubRepo;
    @Mock
    private GameweekRepository gameweekRepo;
    @Mock
    private PlayerGameweekStatsRepository statsRepo;
    @Mock
    private PredictionRepository predictionRepo;
    @Mock
    private TransferLogRepository transferLogRepo;

    private GameweekPhaseService phaseService;
    private GameweekSquadService squadService;

    @BeforeEach
    void setUp() {
        phaseService = new GameweekPhaseService(gameweekRepo);
        squadService = new GameweekSquadService(
                myTeamStateRepo, myTeamPickRepo, myTeamAutoSubRepo,
                gameweekRepo, statsRepo, predictionRepo, transferLogRepo, phaseService
        );
    }

    @Test
    void testSettledSquadReconciliationAndAutoSubs() {
        Gameweek gw8 = new Gameweek();
        gw8.setId(8);
        gw8.setFinished(true);

        when(gameweekRepo.findById(8)).thenReturn(Optional.of(gw8));

        Player p1 = new Player();
        p1.setId(10);
        p1.setWebName("Haaland");
        p1.setElementType((short) 4);

        Player p2 = new Player();
        p2.setId(20);
        p2.setWebName("Gabriel");
        p2.setElementType((short) 2);

        MyTeamPick pick1 = new MyTeamPick();
        pick1.setPlayer(p1);
        pick1.setPosition((short) 1);
        pick1.setMultiplier((short) 2);
        pick1.setIsCaptain(true);

        MyTeamPick pick2 = new MyTeamPick();
        pick2.setPlayer(p2);
        pick2.setPosition((short) 12);
        pick2.setMultiplier((short) 0);

        when(myTeamPickRepo.findByGameweekIdOrderByPositionAsc(8)).thenReturn(List.of(pick1, pick2));

        MyTeamAutoSub autoSub = new MyTeamAutoSub(gw8, p1, p2);
        when(myTeamAutoSubRepo.findByGameweekId(8)).thenReturn(List.of(autoSub));

        PlayerGameweekStats stats1 = new PlayerGameweekStats();
        stats1.setPlayer(p1);
        stats1.setTotalPoints(10);
        stats1.setMinutes(90);

        when(statsRepo.findByGameweekId(8)).thenReturn(List.of(stats1));

        MyTeamState st = new MyTeamState();
        st.setGameweek(gw8);
        st.setGameweekPoints(20);
        st.setPointsOnBench(0);
        when(myTeamStateRepo.findByGameweekId(8)).thenReturn(Optional.of(st));

        TeamCurrentResponse response = squadService.getSquadForGameweek(8);

        assertThat(response.getPhase()).isEqualTo("FINISHED");
        assertThat(response.getIsProjected()).isFalse();
        assertThat(response.getGameweekPoints()).isEqualTo(20);
        assertThat(response.getReconciledGameweekPoints()).isEqualTo(20);
        assertThat(response.getPointsDiscrepancy()).isFalse();

        assertThat(response.getStartingXI()).hasSize(1);
        TeamCurrentResponse.PickDto starter = response.getStartingXI().get(0);
        assertThat(starter.getPlayerId()).isEqualTo(10);
        assertThat(starter.getRawPoints()).isEqualTo(10);
        assertThat(starter.getEffectivePoints()).isEqualTo(20);
        assertThat(starter.getIsAutoSubIn()).isTrue();
        assertThat(starter.getSubbedWithPlayerName()).isEqualTo("Gabriel");

        assertThat(response.getBench()).hasSize(1);
        TeamCurrentResponse.PickDto bencher = response.getBench().get(0);
        assertThat(bencher.getIsAutoSubOut()).isTrue();
        assertThat(bencher.getSubbedWithPlayerName()).isEqualTo("Haaland");
    }

    @Test
    void testProjectedSquadForUpcomingGameweek() {
        Gameweek gw9 = new Gameweek();
        gw9.setId(9);
        gw9.setFinished(false);
        gw9.setIsNext(true);

        when(gameweekRepo.findById(9)).thenReturn(Optional.of(gw9));

        // DB picks for GW9 are empty (pre-deadline)
        when(myTeamPickRepo.findByGameweekIdOrderByPositionAsc(9)).thenReturn(List.of());

        // Settled base picks from GW8
        Gameweek gw8 = new Gameweek();
        gw8.setId(8);
        MyTeamState st8 = new MyTeamState();
        st8.setGameweek(gw8);
        when(myTeamStateRepo.findAll(any(Sort.class))).thenReturn(List.of(st8));
        when(myTeamPickRepo.countByGameweekId(8)).thenReturn(15L);

        Player pOld = new Player();
        pOld.setId(100);
        pOld.setWebName("OldPlayer");

        MyTeamPick basePick = new MyTeamPick();
        basePick.setPlayer(pOld);
        basePick.setPosition((short) 1);
        basePick.setMultiplier((short) 1);

        when(myTeamPickRepo.findByGameweekIdOrderByPositionAsc(8)).thenReturn(List.of(basePick));

        // Confirmed transfer in GW9: OldPlayer -> NewPlayer
        Player pNew = new Player();
        pNew.setId(200);
        pNew.setWebName("NewPlayer");

        TransferLog transfer = new TransferLog();
        transfer.setPlayerOut(pOld);
        transfer.setPlayerIn(pNew);
        when(transferLogRepo.findByGameweekIdOrderByTransferTimeDesc(9)).thenReturn(List.of(transfer));

        TeamCurrentResponse response = squadService.getSquadForGameweek(9);

        assertThat(response.getPhase()).isEqualTo("UPCOMING");
        assertThat(response.getIsProjected()).isTrue();
        assertThat(response.getStartingXI()).hasSize(1);
        assertThat(response.getStartingXI().get(0).getPlayerId()).isEqualTo(200);
        assertThat(response.getStartingXI().get(0).getWebName()).isEqualTo("NewPlayer");
    }
}
