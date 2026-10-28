package com.fplassistant.service;

import com.fplassistant.dto.GameweekDto;
import com.fplassistant.model.Gameweek;
import com.fplassistant.repository.GameweekRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameweekPhaseServiceTest {

    @Mock
    private GameweekRepository gameweekRepo;

    private GameweekPhaseService phaseService;

    @BeforeEach
    void setUp() {
        phaseService = new GameweekPhaseService(gameweekRepo);
    }

    @Test
    void testDeterminePhaseFinished() {
        Gameweek gw = new Gameweek();
        gw.setId(8);
        gw.setFinished(true);

        assertThat(phaseService.determinePhase(gw)).isEqualTo("FINISHED");
    }

    @Test
    void testDeterminePhaseLive() {
        Gameweek gw = new Gameweek();
        gw.setId(8);
        gw.setFinished(false);
        gw.setDeadlineTime(LocalDateTime.now().minusHours(2));

        assertThat(phaseService.determinePhase(gw)).isEqualTo("LIVE");
    }

    @Test
    void testDeterminePhaseUpcoming() {
        Gameweek gw = new Gameweek();
        gw.setId(9);
        gw.setFinished(false);
        gw.setIsNext(true);
        gw.setDeadlineTime(LocalDateTime.now().plusDays(3));

        assertThat(phaseService.determinePhase(gw)).isEqualTo("UPCOMING");
    }

    @Test
    void testDeterminePhaseFuture() {
        Gameweek gw = new Gameweek();
        gw.setId(10);
        gw.setFinished(false);
        gw.setIsNext(false);
        gw.setIsCurrent(false);
        gw.setDeadlineTime(LocalDateTime.now().plusDays(10));

        assertThat(phaseService.determinePhase(gw)).isEqualTo("FUTURE");
    }

    @Test
    void testDefaultHomeGameweekJumpsToNextWhenCurrentFinished() {
        Gameweek gw8 = new Gameweek();
        gw8.setId(8);
        gw8.setIsCurrent(true);
        gw8.setFinished(true);
        gw8.setDeadlineTime(LocalDateTime.now().minusDays(2));

        Gameweek gw9 = new Gameweek();
        gw9.setId(9);
        gw9.setIsNext(true);
        gw9.setFinished(false);
        gw9.setDeadlineTime(LocalDateTime.now().plusDays(3));

        when(gameweekRepo.findAll(any(Sort.class))).thenReturn(List.of(gw8, gw9));

        Integer defaultHome = phaseService.getDefaultHomeGameweekId();
        assertThat(defaultHome).isEqualTo(9);

        List<GameweekDto> dtoList = phaseService.getAllGameweeks();
        assertThat(dtoList).hasSize(2);
        assertThat(dtoList.get(0).getPhase()).isEqualTo("FINISHED");
        assertThat(dtoList.get(0).getIsDefaultHome()).isFalse();
        assertThat(dtoList.get(1).getPhase()).isEqualTo("UPCOMING");
        assertThat(dtoList.get(1).getIsDefaultHome()).isTrue();
    }
}
