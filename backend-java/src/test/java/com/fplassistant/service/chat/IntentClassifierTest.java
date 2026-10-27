package com.fplassistant.service.chat;

import com.fplassistant.model.Intent;
import com.fplassistant.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntentClassifierTest {

    private IntentClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new IntentClassifier();
    }

    @ParameterizedTest(name = "[{index}] Query: \"{0}\" -> Expected: {1}")
    @CsvSource(delimiter = '|', value = {
        // CHIPS_STATUS (queries asking about availability/status)
        "what all are my remaining chips? | CHIPS_STATUS",
        "what chips do i have left? | CHIPS_STATUS",
        "do i have wildcard left? | CHIPS_STATUS",
        "which chips are available? | CHIPS_STATUS",
        "show my chips | CHIPS_STATUS",
        "check chip status | CHIPS_STATUS",
        "any chips remaining? | CHIPS_STATUS",

        // CHIP_STRATEGY (queries asking when/how/timing)
        "when should i play wildcard? | CHIP_STRATEGY",
        "best gameweek for triple captain | CHIP_STRATEGY",
        "chip strategy for upcoming double gameweek | CHIP_STRATEGY",
        "when to use bench boost | CHIP_STRATEGY",
        "how should i use my chips? | CHIP_STRATEGY",

        // TRANSFER_ADVICE
        "hey, what transfer should i make this week? | TRANSFER_ADVICE",
        "who should i transfer in? | TRANSFER_ADVICE",
        "should i sell haaland? | TRANSFER_ADVICE",
        "recommend a transfer for gw9 | TRANSFER_ADVICE",
        "who should i buy for diaz? | TRANSFER_ADVICE",
        "transfer out pickford | TRANSFER_ADVICE",
        "best moves for next gameweek | TRANSFER_ADVICE",

        // CAPTAIN_ADVICE
        "who should i captain? | CAPTAIN_ADVICE",
        "who is the best captain pick this week? | CAPTAIN_ADVICE",
        "vice captain recommendation | CAPTAIN_ADVICE",
        "who gets the armband? | CAPTAIN_ADVICE",
        "should i captain salah or haaland? | CAPTAIN_ADVICE",

        // SQUAD_WEAKNESS
        "what are my squad's weaknesses? | SQUAD_WEAKNESS",
        "rate my team | SQUAD_WEAKNESS",
        "where is my team weak? | SQUAD_WEAKNESS",
        "squad review | SQUAD_WEAKNESS",
        "check my squad health | SQUAD_WEAKNESS",

        // DIFFERENTIALS
        "give me some differential picks | DIFFERENTIALS",
        "who are good differentials under 10%? | DIFFERENTIALS",
        "hidden gems for midfield | DIFFERENTIALS",
        "any low ownership punts? | DIFFERENTIALS",

        // FIXTURE_ANALYSIS
        "who has the easiest fixtures? | FIXTURE_ANALYSIS",
        "arsenal fixture run | FIXTURE_ANALYSIS",
        "show me upcoming fixtures | FIXTURE_ANALYSIS",
        "fdr difficulty ratings for next 4 gameweeks | FIXTURE_ANALYSIS",

        // GENERAL_FPL
        "when is the deadline for gw9? | GENERAL_FPL",
        "how do bonus points work? | GENERAL_FPL",
        "how does price change work in fpl? | GENERAL_FPL",

        // OFF_TOPIC
        "what is the recipe for pasta? | OFF_TOPIC",
        "what's the weather like in London? | OFF_TOPIC",
        "write a python script to parse csv | OFF_TOPIC"
    })
    void testIntentClassification(String query, Intent expectedIntent) {
        Intent actual = classifier.classify(query, Collections.emptyList());
        assertThat(actual).isEqualTo(expectedIntent);
    }

    @ParameterizedTest(name = "Compare: \"{0}\" -> PLAYER_COMPARE")
    @CsvSource(delimiter = '|', value = {
        "compare salah and palmer",
        "salah vs haaland",
        "who is better, saka or son?"
    })
    void testPlayerCompareClassification(String query) {
        Player p1 = new Player();
        p1.setWebName("Salah");
        Player p2 = new Player();
        p2.setWebName("Haaland");
        Intent actual = classifier.classify(query, List.of(p1, p2));
        assertThat(actual).isEqualTo(Intent.PLAYER_COMPARE);
    }
}
