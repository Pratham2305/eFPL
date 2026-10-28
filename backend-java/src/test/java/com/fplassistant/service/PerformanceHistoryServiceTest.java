package com.fplassistant.service;

import com.fplassistant.dto.PerformanceHistoryResponse;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceHistoryServiceTest {

    @Mock
    private MyTeamStateRepository myTeamStateRepo;

    @Mock
    private MyTeamPickRepository myTeamPickRepo;

    @Mock
    private PlayerGameweekStatsRepository statsRepo;

    @Mock
    private GameweekRepository gameweekRepo;

    private PerformanceHistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new PerformanceHistoryService(myTeamStateRepo, myTeamPickRepo, statsRepo, gameweekRepo);
    }

    @Test
    void testPerformanceHistoryCalculations() {
        Gameweek gw1 = new Gameweek();
        gw1.setId(1);
        gw1.setAverageEntryScore(50);
        gw1.setHighestScore(90);

        Player captain = new Player();
        captain.setId(101);
        captain.setWebName("Salah");

        MyTeamPick captainPick = new MyTeamPick();
        captainPick.setPlayer(captain);
        captainPick.setMultiplier((short) 2);
        captainPick.setIsCaptain(true);
        captainPick.setPosition((short) 1);

        PlayerGameweekStats captainStats = new PlayerGameweekStats();
        captainStats.setTotalPoints(12);

        MyTeamState st1 = new MyTeamState();
        st1.setGameweek(gw1);
        st1.setGameweekPoints(70);
        st1.setEventTransfers(1);
        st1.setEventTransfersCost(4); // net = 66
        st1.setOverallPoints(66);
        st1.setOverallRank(1200000);
        st1.setGameweekRank(500000);
        st1.setPointsOnBench(6);
        st1.setBank(15);
        st1.setTeamValue(1010);
        st1.setActiveChip("3xc");

        when(myTeamStateRepo.findAll(any(Sort.class))).thenReturn(List.of(st1));
        when(myTeamPickRepo.findByGameweekIdOrderByPositionAsc(1)).thenReturn(List.of(captainPick));
        when(statsRepo.findFirstByPlayerIdAndGameweekId(101, 1)).thenReturn(Optional.of(captainStats));

        PerformanceHistoryResponse response = historyService.getPerformanceHistory();

        assertThat(response).isNotNull();
        assertThat(response.getGameweeks()).hasSize(1);
        PerformanceHistoryResponse.GameweekHistoryDto gwDto = response.getGameweeks().get(0);
        assertThat(gwDto.getPoints()).isEqualTo(70);
        assertThat(gwDto.getNetPoints()).isEqualTo(66);
        assertThat(gwDto.getTransferCost()).isEqualTo(4);
        assertThat(gwDto.getCaptainName()).isEqualTo("Salah");
        assertThat(gwDto.getCaptainPoints()).isEqualTo(24);
        assertThat(gwDto.getBank()).isEqualTo(1.5);
        assertThat(gwDto.getTeamValue()).isEqualTo(101.0);

        PerformanceHistoryResponse.SeasonKpiDto summary = response.getSummary();
        assertThat(summary.getTotalPoints()).isEqualTo(66);
        assertThat(summary.getAverageGameweekScore()).isEqualTo(66.0);
        assertThat(summary.getAverageLeagueScore()).isEqualTo(50.0);
        assertThat(summary.getScoreVersusLeagueAvg()).isEqualTo(16.0);
        assertThat(summary.getTotalTransferCost()).isEqualTo(4);
        assertThat(summary.getTotalBenchPoints()).isEqualTo(6);
        assertThat(summary.getBestGameweekRank()).isEqualTo(500000);
        assertThat(summary.getCaptainEfficiency().getTotalCaptainPoints()).isEqualTo(24);
        assertThat(summary.getCaptainEfficiency().getOptimalCaptainHits()).isEqualTo(1);

        assertThat(response.getChipsTimeline()).hasSize(1);
        assertThat(response.getChipsTimeline().get(0).getChipName()).isEqualTo("Triple Captain");

        assertThat(response.getDeterministicInsights()).isNotEmpty();
    }
}
