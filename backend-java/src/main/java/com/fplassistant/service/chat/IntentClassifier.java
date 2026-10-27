package com.fplassistant.service.chat;

import com.fplassistant.model.Intent;
import com.fplassistant.model.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntentClassifier {

    public Intent classify(String query, List<Player> resolvedPlayers) {
        if (query == null || query.isBlank()) {
            return Intent.OFF_TOPIC;
        }

        String q = PlayerResolver.normalize(query);

        // 1. Off-topic check
        if (isOffTopic(q)) {
            return Intent.OFF_TOPIC;
        }

        // 2. Chip Strategy vs Chip Status
        // If query asks "when", "strategy", "timing", "planning", "which gameweek", "how to play"
        boolean mentionsChip = q.matches(".*\\b(chip|chips|wildcard|free hit|bench boost|triple captain|tc|bb|fh|wc)\\b.*");
        if (mentionsChip) {
            if (q.matches(".*\\b(when|timing|strategy|best gameweek|which gw|plan|play my|activate|trigger|save|how should i use|best time)\\b.*")) {
                return Intent.CHIP_STRATEGY;
            }
            if (q.matches(".*\\b(remaining|what|show|check|available|left|have|list|status|do i have|which chips|any chips)\\b.*")) {
                return Intent.CHIPS_STATUS;
            }
        }

        // 3. Captaincy advice
        if (q.matches(".*\\b(captain|captaincy|vice[- ]captain|armband|vc|vice)\\b.*")) {
            return Intent.CAPTAIN_ADVICE;
        }

        // 4. Player Compare (if explicitly compare / vs / versus, or 2+ players resolved and compare keywords)
        if (resolvedPlayers != null && resolvedPlayers.size() >= 2) {
            if (q.matches(".*\\b(compare|vs|versus|or|better|which one|between)\\b.*")) {
                return Intent.PLAYER_COMPARE;
            }
        }
        if (q.matches(".*\\b(compare|versus)\\b.*") && resolvedPlayers != null && !resolvedPlayers.isEmpty()) {
            return Intent.PLAYER_COMPARE;
        }

        // 5. Transfer advice
        if (q.matches(".*\\b(transfer|transfers|sell|buy|swap|replace|bring in|get rid of|move|moves|signing|signings|replace)\\b.*")) {
            return Intent.TRANSFER_ADVICE;
        }

        // 6. Squad weaknesses & review
        if (q.matches(".*\\b(weakness|weaknesses|rate my team|rmt|squad review|evaluate|bench issue|problem area|rate my squad|improve my team|squad health|weak link|weak spots)\\b.*")) {
            return Intent.SQUAD_WEAKNESS;
        }

        // 7. Differentials
        if (q.matches(".*\\b(differential|differentials|low ownership|hidden gem|gems|punt|punts)\\b.*")) {
            return Intent.DIFFERENTIALS;
        }

        // 8. Fixtures & FDR
        if (q.matches(".*\\b(fixture|fixtures|fdr|difficulty|schedule|run of games|swing|blank|double gameweek|bgw|dgw)\\b.*")) {
            return Intent.FIXTURE_ANALYSIS;
        }

        // 9. Single Player Info (if exactly 1 player resolved and asking for info/stats)
        if (resolvedPlayers != null && resolvedPlayers.size() == 1) {
            if (q.matches(".*\\b(injured|injury|news|points|xp|expected|stats|form|minutes|status|price|cost|play|start|about|doing|how is)\\b.*")
                    || q.split("\\s+").length <= 4) {
                return Intent.PLAYER_INFO;
            }
        }

        // 10. General FPL rules & deadline
        if (q.matches(".*\\b(deadline|rules|bonus points|bps|price change|price rise|price fall|how does fpl work|when does the gameweek start|chip rules|scoring system)\\b.*")) {
            return Intent.GENERAL_FPL;
        }

        // Fallback checks
        if (q.contains("squad") || q.contains("my team")) {
            return Intent.SQUAD_WEAKNESS;
        }

        return Intent.OFF_TOPIC;
    }

    private boolean isOffTopic(String q) {
        return q.matches(".*\\b(weather|recipe|cook|bake|movie|song|music|politics|president|capital of|tell me a joke|write a poem|python script|java code)\\b.*");
    }
}
