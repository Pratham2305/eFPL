package com.fplassistant.service.chat;

import com.fplassistant.model.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicAnswerRendererTest {

    private DeterministicAnswerRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new DeterministicAnswerRenderer();
    }

    @Test
    void testRenderChipsStatus() {
        Map<String, Object> context = Map.of(
                "gameweek_info", Map.of("next_gameweek", 9),
                "squad_summary", Map.of(
                        "chips_remaining", List.of("Wildcard", "Free Hit", "Triple Captain"),
                        "chips_used", List.of("Bench Boost"),
                        "bank_millions", 0.5,
                        "free_transfers", 1
                )
        );

        String result = renderer.render(Intent.CHIPS_STATUS, context, "what all are my remaining chips?", Collections.emptyList());

        assertThat(result).contains("You have **3** chip(s) remaining");
        assertThat(result).contains("Wildcard, Free Hit, Triple Captain");
        assertThat(result).contains("Used: Bench Boost");
        assertThat(result).contains("£0.5m");
        assertThat(result).contains("1 free transfer(s)");
    }

    @Test
    void testRenderTransferAdvice() {
        Map<String, Object> context = Map.of(
                "gameweek_info", Map.of("next_gameweek", 9),
                "squad_summary", Map.of("bank_millions", 0.5, "free_transfers", 1),
                "top_transfer_recommendations", List.of(
                        Map.of(
                                "player_out_name", "Pickford",
                                "player_in_name", "Raya",
                                "net_xp_1gw", 1.85,
                                "net_xp_3gw", 4.20,
                                "hit_cost", 0,
                                "verdict", "RECOMMENDED"
                        )
                )
        );

        String result = renderer.render(Intent.TRANSFER_ADVICE, context, "hey, what transfer should i make this week?", Collections.emptyList());

        assertThat(result).contains("Pickford");
        assertThat(result).contains("Raya");
        assertThat(result).contains("+1.85");
        assertThat(result).contains("RECOMMENDED");
        assertThat(result).contains("Free");
    }

    @Test
    void testRenderCaptainAdvice() {
        Map<String, Object> context = Map.of(
                "gameweek_info", Map.of("next_gameweek", 9),
                "captaincy_rankings", List.of(
                        Map.of(
                                "player_name", "Salah",
                                "position", "MID",
                                "opponent_short_name", "ARS",
                                "fdr", 4,
                                "expected_points", 7.6,
                                "risk_profile", "balanced"
                        )
                )
        );

        String result = renderer.render(Intent.CAPTAIN_ADVICE, context, "who should i captain?", Collections.emptyList());

        assertThat(result).contains("Salah");
        assertThat(result).contains("7.6 xP");
        assertThat(result).contains("ARS");
    }
}
