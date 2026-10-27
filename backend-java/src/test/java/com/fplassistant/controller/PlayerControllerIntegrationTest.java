package com.fplassistant.controller;

import com.fplassistant.model.Gameweek;
import com.fplassistant.model.Player;
import com.fplassistant.model.Team;
import com.fplassistant.repository.GameweekRepository;
import com.fplassistant.repository.PlayerRepository;
import com.fplassistant.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlayerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GameweekRepository gameweekRepository;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        if (gameweekRepository.count() == 0) {
            Gameweek gw = new Gameweek();
            gw.setId(1);
            gw.setName("Gameweek 1");
            gw.setIsNext(true);
            gameweekRepository.save(gw);
        }

        Team team = teamRepository.findById(1).orElseGet(() -> {
            Team t = new Team();
            t.setId(1);
            t.setName("Arsenal");
            t.setShortName("ARS");
            t.setCode(3);
            return teamRepository.save(t);
        });

        testPlayer = playerRepository.findById(1).orElseGet(() -> {
            Player p = new Player();
            p.setId(1);
            p.setTeam(team);
            p.setFirstName("Bukayo");
            p.setSecondName("Saka");
            p.setWebName("Saka");
            p.setElementType((short) 3);
            p.setNowCost(100); // £10.0m
            p.setStatus("a");
            p.setSelectedByPercent(new BigDecimal("35.5"));
            p.setTotalPoints(88);
            return playerRepository.save(p);
        });
    }

    @Test
    void testGetPlayersHappyPath() throws Exception {
        mockMvc.perform(get("/api/players")
                        .param("search", "saka")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].webName").value("Saka"))
                .andExpect(jsonPath("$.content[0].teamShortName").value("ARS"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(25));
    }

    @Test
    void testGetPlayerDetailHappyPath() throws Exception {
        mockMvc.perform(get("/api/players/" + testPlayer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.id").value(testPlayer.getId()))
                .andExpect(jsonPath("$.summary.webName").value("Saka"))
                .andExpect(jsonPath("$.upcomingFixtures").isArray())
                .andExpect(jsonPath("$.historicalStats").isArray());
    }

    @Test
    void testGetPlayerDetailNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/players/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(containsString("Player not found")));
    }

    @Test
    void testGetPlayersInvalidValidationReturns400() throws Exception {
        // Negative page index
        mockMvc.perform(get("/api/players")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Invalid sort key
        mockMvc.perform(get("/api/players")
                        .param("sortBy", "invalid_column"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Invalid size > 100
        mockMvc.perform(get("/api/players")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testGetMeta() throws Exception {
        mockMvc.perform(get("/api/players/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teams").isArray())
                .andExpect(jsonPath("$.positions", hasSize(4)))
                .andExpect(jsonPath("$.priceRange").exists())
                .andExpect(jsonPath("$.ownershipRange").exists())
                .andExpect(jsonPath("$.totalPlayers").value(greaterThanOrEqualTo(1)));
    }
}
