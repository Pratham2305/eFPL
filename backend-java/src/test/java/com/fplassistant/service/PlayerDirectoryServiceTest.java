package com.fplassistant.service;

import com.fplassistant.dto.PlayerDetailDto;
import com.fplassistant.dto.PlayerMetaDto;
import com.fplassistant.dto.PlayerPageResponse;
import com.fplassistant.dto.PlayerSummaryDto;
import com.fplassistant.exception.ResourceNotFoundException;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerDirectoryServiceTest {

    @Mock
    private PlayerRepository playerRepo;
    @Mock
    private TeamRepository teamRepo;
    @Mock
    private GameweekRepository gameweekRepo;
    @Mock
    private FixtureRepository fixtureRepo;
    @Mock
    private PredictionRepository predictionRepo;
    @Mock
    private PlayerGameweekStatsRepository playerGameweekStatsRepo;
    @Mock
    private MyTeamStateRepository myTeamStateRepo;
    @Mock
    private MyTeamPickRepository myTeamPickRepo;

    @InjectMocks
    private PlayerDirectoryService service;

    private Team arsenal;
    private Team liverpool;
    private Player p1;
    private Player p2;
    private Player p3;
    private Gameweek gw10;

    @BeforeEach
    void setUp() {
        arsenal = new Team();
        arsenal.setId(1);
        arsenal.setName("Arsenal");
        arsenal.setShortName("ARS");
        arsenal.setCode(3);

        liverpool = new Team();
        liverpool.setId(2);
        liverpool.setName("Liverpool");
        liverpool.setShortName("LIV");
        liverpool.setCode(14);

        p1 = new Player();
        p1.setId(101);
        p1.setTeam(arsenal);
        p1.setFirstName("Martin");
        p1.setSecondName("Ødegaard");
        p1.setWebName("Ødegaard");
        p1.setElementType((short) 3); // MID
        p1.setNowCost(85); // £8.5m
        p1.setStatus("a");
        p1.setSelectedByPercent(new BigDecimal("12.5"));
        p1.setTotalPoints(65);

        p2 = new Player();
        p2.setId(102);
        p2.setTeam(liverpool);
        p2.setFirstName("Mohamed");
        p2.setSecondName("Salah");
        p2.setWebName("Salah");
        p2.setElementType((short) 3); // MID
        p2.setNowCost(128); // £12.8m
        p2.setStatus("a");
        p2.setSelectedByPercent(new BigDecimal("45.0"));
        p2.setTotalPoints(110);

        p3 = new Player();
        p3.setId(103);
        p3.setTeam(arsenal);
        p3.setFirstName("William");
        p3.setSecondName("Saliba");
        p3.setWebName("Saliba");
        p3.setElementType((short) 2); // DEF
        p3.setNowCost(60); // £6.0m
        p3.setStatus("d");
        p3.setSelectedByPercent(new BigDecimal("28.0"));
        p3.setTotalPoints(50);

        gw10 = new Gameweek();
        gw10.setId(10);
        gw10.setName("Gameweek 10");
        gw10.setIsCurrent(false);
        gw10.setIsNext(true);
    }

    private void stubBaseData() {
        when(gameweekRepo.findByIsCurrentTrue()).thenReturn(Optional.empty());
        when(gameweekRepo.findByIsNextTrue()).thenReturn(Optional.of(gw10));
        when(fixtureRepo.findByGameweekId(10)).thenReturn(Collections.emptyList());
        when(predictionRepo.findByGameweekId(10)).thenReturn(Collections.emptyList());
        when(myTeamStateRepo.findByGameweekId(10)).thenReturn(Optional.empty());
        when(myTeamStateRepo.findTopByOrderByGameweekIdDesc()).thenReturn(Optional.empty());
        when(teamRepo.findAll()).thenReturn(List.of(arsenal, liverpool));
        when(playerRepo.findAll()).thenReturn(List.of(p1, p2, p3));
    }

    @Test
    void testSearchWithAccentFoldingAndMultiToken() {
        stubBaseData();

        // Search "martin odegaard" (unaccented) should match Martin Ødegaard
        PlayerPageResponse resp = service.getPlayers(
                "martin odegaard", null, null, null, null, null, null, null, null,
                "total_points", "desc", 0, 25);

        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getContent().get(0).getId()).isEqualTo(101);
        assertThat(resp.getContent().get(0).getWebName()).isEqualTo("Ødegaard");

        // Non matching token returns empty
        PlayerPageResponse emptyResp = service.getPlayers(
                "martin haaland", null, null, null, null, null, null, null, null,
                "total_points", "desc", 0, 25);
        assertThat(emptyResp.getContent()).isEmpty();
    }

    @Test
    void testPriceFilterConversionToTenths() {
        stubBaseData();

        // minPrice 7.0 (£7.0m = 70 tenths), maxPrice 9.0 (£9.0m = 90 tenths) -> should match p1 (85)
        PlayerPageResponse resp = service.getPlayers(
                null, null, null, 7.0, 9.0, null, null, null, null,
                "total_points", "desc", 0, 25);

        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getContent().get(0).getId()).isEqualTo(101);
    }

    @Test
    void testPositionAndStatusFiltering() {
        stubBaseData();

        // Filter only DEF (elementType 2)
        PlayerPageResponse respDef = service.getPlayers(
                null, null, List.of((short) 2), null, null, null, null, null, null,
                "total_points", "desc", 0, 25);
        assertThat(respDef.getContent()).hasSize(1);
        assertThat(respDef.getContent().get(0).getId()).isEqualTo(103);

        // Filter status "d"
        PlayerPageResponse respStatus = service.getPlayers(
                null, null, null, null, null, List.of("d"), null, null, null,
                "total_points", "desc", 0, 25);
        assertThat(respStatus.getContent()).hasSize(1);
        assertThat(respStatus.getContent().get(0).getId()).isEqualTo(103);
    }

    @Test
    void testNullsAlwaysLastSorting() {
        when(gameweekRepo.findByIsCurrentTrue()).thenReturn(Optional.empty());
        when(gameweekRepo.findByIsNextTrue()).thenReturn(Optional.of(gw10));
        when(fixtureRepo.findByGameweekId(10)).thenReturn(Collections.emptyList());

        // Setup predictions: p1 has 6.5 xP, p2 has null xP, p3 has 4.2 xP
        Prediction pred1 = new Prediction();
        pred1.setPlayer(p1);
        pred1.setGameweek(gw10);
        pred1.setExpectedPoints(new BigDecimal("6.5"));

        Prediction pred3 = new Prediction();
        pred3.setPlayer(p3);
        pred3.setGameweek(gw10);
        pred3.setExpectedPoints(new BigDecimal("4.2"));

        when(predictionRepo.findByGameweekId(10)).thenReturn(List.of(pred1, pred3));
        when(myTeamStateRepo.findByGameweekId(10)).thenReturn(Optional.empty());
        when(myTeamStateRepo.findTopByOrderByGameweekIdDesc()).thenReturn(Optional.empty());
        when(teamRepo.findAll()).thenReturn(List.of(arsenal, liverpool));
        when(playerRepo.findAll()).thenReturn(List.of(p1, p2, p3));

        // Sort descending: p1 (6.5), p3 (4.2), p2 (null last)
        PlayerPageResponse descResp = service.getPlayers(
                null, null, null, null, null, null, null, null, null,
                "xp_next", "desc", 0, 25);
        assertThat(descResp.getContent().get(0).getId()).isEqualTo(101);
        assertThat(descResp.getContent().get(1).getId()).isEqualTo(103);
        assertThat(descResp.getContent().get(2).getId()).isEqualTo(102);
        assertThat(descResp.getContent().get(2).getXpNext()).isNull();

        // Sort ascending: p3 (4.2), p1 (6.5), p2 (null still last!)
        PlayerPageResponse ascResp = service.getPlayers(
                null, null, null, null, null, null, null, null, null,
                "xp_next", "asc", 0, 25);
        assertThat(ascResp.getContent().get(0).getId()).isEqualTo(103);
        assertThat(ascResp.getContent().get(1).getId()).isEqualTo(101);
        assertThat(ascResp.getContent().get(2).getId()).isEqualTo(102);
        assertThat(ascResp.getContent().get(2).getXpNext()).isNull();
    }

    @Test
    void testValidationErrors() {
        assertThatThrownBy(() -> service.getPlayers(
                null, null, null, null, null, null, null, null, null,
                "total_points", "desc", -1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page index");

        assertThatThrownBy(() -> service.getPlayers(
                null, null, null, null, null, null, null, null, null,
                "total_points", "desc", 0, 150))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size");

        assertThatThrownBy(() -> service.getPlayers(
                null, null, null, null, null, null, null, null, null,
                "invalid_field", "desc", 0, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort field");

        assertThatThrownBy(() -> service.getPlayers(
                null, null, null, 10.0, 5.0, null, null, null, null,
                "total_points", "desc", 0, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxPrice cannot be less than minPrice");
    }

    @Test
    void testPlayerDetailNotFound() {
        when(playerRepo.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPlayerDetail(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    void testGetMeta() {
        when(teamRepo.findAll()).thenReturn(List.of(arsenal, liverpool));
        when(playerRepo.findAll()).thenReturn(List.of(p1, p2, p3));
        when(gameweekRepo.findByIsCurrentTrue()).thenReturn(Optional.empty());
        when(gameweekRepo.findByIsNextTrue()).thenReturn(Optional.of(gw10));

        PlayerMetaDto meta = service.getMeta();
        assertThat(meta.getTotalPlayers()).isEqualTo(3);
        assertThat(meta.getTeams()).hasSize(2);
        assertThat(meta.getPositions()).hasSize(4);
        assertThat(meta.getPriceRange().getMin()).isEqualTo(6.0);
        assertThat(meta.getPriceRange().getMax()).isEqualTo(12.8);
        assertThat(meta.getNextGameweek()).isEqualTo(10);
    }
}
