package com.fplassistant.service.chat;

import com.fplassistant.model.Intent;
import com.fplassistant.model.Player;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DeterministicAnswerRenderer {

    @SuppressWarnings("unchecked")
    public String render(Intent intent, Map<String, Object> context, String query, List<Player> resolvedPlayers) {
        Map<String, Object> squadSummary = (Map<String, Object>) context.getOrDefault("squad_summary", Collections.emptyMap());
        Map<String, Object> gwInfo = (Map<String, Object>) context.getOrDefault("gameweek_info", Collections.emptyMap());
        int nextGw = ((Number) gwInfo.getOrDefault("next_gameweek", 9)).intValue();

        List<String> remainingChips = (List<String>) squadSummary.getOrDefault("chips_remaining", List.of("Wildcard", "Free Hit", "Triple Captain", "Bench Boost"));
        List<String> usedChips = (List<String>) squadSummary.getOrDefault("chips_used", Collections.emptyList());
        double bank = ((Number) squadSummary.getOrDefault("bank_millions", 0.0)).doubleValue();
        int ft = ((Number) squadSummary.getOrDefault("free_transfers", 1)).intValue();
        int rank = ((Number) squadSummary.getOrDefault("overall_rank", 0)).intValue();

        return switch (intent) {
            case CHIPS_STATUS -> renderChipsStatus(remainingChips, usedChips, ft, bank, nextGw);
            case CHIP_STRATEGY -> renderChipStrategy(remainingChips, usedChips, nextGw);
            case TRANSFER_ADVICE -> renderTransferAdvice(context, nextGw, bank, ft);
            case CAPTAIN_ADVICE -> renderCaptainAdvice(context, nextGw);
            case SQUAD_WEAKNESS -> renderSquadWeakness(context);
            case DIFFERENTIALS -> renderDifferentials(context);
            case PLAYER_COMPARE -> renderPlayerCompare(context, resolvedPlayers, nextGw);
            case PLAYER_INFO -> renderPlayerInfo(context, resolvedPlayers, nextGw);
            case FIXTURE_ANALYSIS -> renderFixtureAnalysis(context, nextGw);
            case GENERAL_FPL -> renderGeneralFpl(query);
            case OFF_TOPIC -> renderOffTopic();
        };
    }

    private String renderChipsStatus(List<String> remaining, List<String> used, int ft, double bank, int nextGw) {
        StringBuilder sb = new StringBuilder();
        sb.append("You have **").append(remaining.size()).append("** chip(s) remaining: ");
        if (remaining.isEmpty()) {
            sb.append("None.");
        } else {
            sb.append("**").append(String.join(", ", remaining)).append("**");
        }

        if (!used.isEmpty()) {
            sb.append(" *(Used: ").append(String.join(", ", used)).append(")*");
        }

        sb.append(". Heading into **Gameweek ").append(nextGw).append("**, you have **")
          .append(ft).append(" free transfer(s)** and **£").append(String.format(Locale.US, "%.1f", bank))
          .append("m** in the bank.");

        return sb.toString();
    }

    private String renderChipStrategy(List<String> remaining, List<String> used, int nextGw) {
        StringBuilder sb = new StringBuilder();
        sb.append("### CHIP STRATEGY RECOMMENDATIONS (GW ").append(nextGw).append("+)\n\n");
        sb.append("You currently have **").append(remaining.size()).append("** available chips: **")
          .append(String.join(", ", remaining)).append("**.\n\n");

        if (remaining.contains("Triple Captain")) {
            sb.append("- **Triple Captain**: Best preserved for confirmed Double Gameweeks (DGW) where premium assets (e.g., Haaland, Salah, Palmer) play twice in one gameweek.\n");
        }
        if (remaining.contains("Free Hit")) {
            sb.append("- **Free Hit**: Maximize utility by saving for major Blank Gameweeks (BGW) with cup postponements or big Double Gameweeks later in the season.\n");
        }
        if (remaining.contains("Wildcard")) {
            sb.append("- **Wildcard**: Activate during major fixture swing windows (e.g. GW 10-12) to restructure team depth and clear non-playing bench deadweight without points deductions.\n");
        }
        if (remaining.contains("Bench Boost")) {
            sb.append("- **Bench Boost**: Coordinate with a prior Wildcard ahead of a substantial Double Gameweek to ensure 15 active double-playing starters.\n");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderTransferAdvice(Map<String, Object> context, int nextGw, double bank, int ft) {
        List<Map<String, Object>> recommendations = (List<Map<String, Object>>) context.get("top_transfer_recommendations");

        StringBuilder sb = new StringBuilder();
        sb.append("### RECOMMENDATION\n\n");
        sb.append("Here are the top data-driven transfer moves evaluated for **GW ").append(nextGw)
          .append("** (Bank: £").append(String.format(Locale.US, "%.1f", bank)).append("m, Free Transfers: ").append(ft).append("):\n\n");

        if (recommendations == null || recommendations.isEmpty()) {
            sb.append("No immediate urgent transfer needed. It is recommended to **roll the free transfer** to have 2 FTs next week.\n\n");
        } else {
            sb.append("| Player Out | Player In | 1-GW Net | 3-GW Net | Cost/Hit | Verdict |\n");
            sb.append("|:---|:---|:---:|:---:|:---:|:---:|\n");

            int count = 0;
            for (Map<String, Object> t : recommendations) {
                String outName = extractPlayerName(t, "player_out_name", "transfer_out");
                String inName = extractPlayerName(t, "player_in_name", "transfer_in");

                double net1 = extractDouble(t, "net_xp_1gw", "xp_gain_1");
                double net3 = extractDouble(t, "net_xp_3gw", "xp_gain_3", "net_value");
                int hit = ((Number) t.getOrDefault("hit_cost", 0)).intValue();
                String verdict = String.valueOf(t.getOrDefault("verdict", "RECOMMENDED"));

                sb.append("| ").append(outName)
                  .append(" | ").append(inName)
                  .append(" | ").append(net1 >= 0 ? "+" : "").append(String.format(Locale.US, "%.2f", net1))
                  .append(" | ").append(net3 >= 0 ? "+" : "").append(String.format(Locale.US, "%.2f", net3))
                  .append(" | ").append(hit > 0 ? ("-" + hit + " pts") : "Free")
                  .append(" | **").append(verdict).append("** |\n");

                count++;
                if (count >= 3) break;
            }
            sb.append("\n");
        }

        sb.append("### CONFIDENCE\n**Medium-High** — Based on baseline heuristic xP models factoring form, fixture FDR, and position baselines.\n\n");
        sb.append("### RISKS\nMonitor press conferences for late injury updates and team news before lock deadline.\n\n");
        sb.append("### ALTERNATIVES\nIf satisfied with your starting XI, **bank the transfer** to gain maximum tactical flexibility next gameweek.\n");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderCaptainAdvice(Map<String, Object> context, int nextGw) {
        List<Map<String, Object>> rankings = (List<Map<String, Object>>) context.get("captaincy_rankings");

        StringBuilder sb = new StringBuilder();
        sb.append("### RECOMMENDATION\n\n");

        if (rankings == null || rankings.isEmpty()) {
            sb.append("Prioritize your highest expected-points asset from your starting XI with a home fixture.\n\n");
        } else {
            Map<String, Object> topPick = rankings.get(0);
            String topName = extractPlayerName(topPick, "player_name", "web_name");
            Object topXp = topPick.getOrDefault("expected_points", "6.0");
            Object topOpp = topPick.getOrDefault("opponent_short_name", topPick.getOrDefault("opponent", "Opponent"));

            sb.append("Armband Recommendation for **GW ").append(nextGw).append("**: **")
              .append(topName)
              .append("** (Projected: **").append(topXp)
              .append(" xP** vs ").append(topOpp)
              .append(").\n\n");

            sb.append("### TOP STARTING XI CANDIDATES\n\n");
            sb.append("| Candidate | Pos | Opponent | FDR | Projected xP | Risk Profile |\n");
            sb.append("|:---|:---:|:---:|:---:|:---:|:---:|\n");

            int count = 0;
            for (Map<String, Object> c : rankings) {
                String cName = extractPlayerName(c, "player_name", "web_name");
                Object cOpp = c.getOrDefault("opponent_short_name", c.getOrDefault("opponent", "-"));
                sb.append("| ").append(cName)
                  .append(" | ").append(c.getOrDefault("position", "MID"))
                  .append(" | ").append(cOpp)
                  .append(" | ").append(c.getOrDefault("fdr", 3))
                  .append(" | ").append(c.getOrDefault("expected_points", 0.0))
                  .append(" | `").append(c.getOrDefault("risk_profile", "balanced")).append("` |\n");

                count++;
                if (count >= 3) break;
            }
            sb.append("\n");

            if (rankings.size() > 1) {
                Map<String, Object> vc = rankings.get(1);
                String vcName = extractPlayerName(vc, "player_name", "web_name");
                sb.append("### VICE-CAPTAIN\n**").append(vcName)
                  .append("** provides optimal floor coverage as vice-captain.\n\n");
            }
        }

        sb.append("### RISKS\nAway fixtures and tight European rotation schedules can impact ceiling output.\n");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderSquadWeakness(Map<String, Object> context) {
        Map<String, Object> weaknesses = (Map<String, Object>) context.get("squad_weaknesses");
        StringBuilder sb = new StringBuilder();
        sb.append("### SQUAD HEALTH & WEAKNESS AUDIT\n\n");

        if (weaknesses == null || !weaknesses.containsKey("findings")) {
            sb.append("Squad analysis shows balanced expected points distribution across starters, with no severe injury flags.\n");
            return sb.toString();
        }

        Object rawHealth = weaknesses.get("squadHealth");
        if (rawHealth == null) rawHealth = weaknesses.get("squad_health");
        Object healthScore = 75;
        if (rawHealth instanceof Map<?, ?> healthMap && healthMap.containsKey("score")) {
            healthScore = healthMap.get("score");
        } else if (rawHealth != null) {
            healthScore = rawHealth;
        }

        sb.append("Current Squad Health Score: **").append(healthScore).append("/100**\n\n");
        sb.append("Key Weaknesses & Opportunities:\n\n");

        List<Map<String, Object>> findings = (List<Map<String, Object>>) weaknesses.get("findings");
        int count = 0;
        for (Map<String, Object> f : findings) {
            String sev = String.valueOf(f.getOrDefault("severity", "WARNING")).toUpperCase();
            String title = String.valueOf(f.getOrDefault("title", f.getOrDefault("issue", "Squad Optimization finding")));
            String recommendation = String.valueOf(f.getOrDefault("recommendation", f.getOrDefault("suggestion", "Consider evaluating squad alternatives.")));

            sb.append("- **[").append(sev).append("] ").append(title).append("**: ")
              .append(recommendation).append("\n");

            count++;
            if (count >= 5) break;
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderDifferentials(Map<String, Object> context) {
        List<Map<String, Object>> diffs = (List<Map<String, Object>>) context.get("differentials");
        StringBuilder sb = new StringBuilder();
        sb.append("### TOP DIFFERENTIAL PICKS (< 10% Ownership)\n\n");

        if (diffs == null || diffs.isEmpty()) {
            sb.append("No differential targets currently meeting criteria.\n");
            return sb.toString();
        }

        sb.append("| Player | Team | Pos | Cost | Ownership | Projected xP |\n");
        sb.append("|:---|:---|:---:|:---:|:---:|:---:|\n");

        int count = 0;
        for (Map<String, Object> d : diffs) {
            String pName = extractPlayerName(d, "player_name", "web_name");
            sb.append("| ").append(pName)
              .append(" | ").append(d.getOrDefault("team_short_name", d.getOrDefault("team_short", "-")))
              .append(" | ").append(d.getOrDefault("position", "MID"))
              .append(" | £").append(d.getOrDefault("cost", d.getOrDefault("now_cost", 6.0))).append("m")
              .append(" | ").append(d.getOrDefault("ownership_percent", 5.0)).append("%")
              .append(" | **").append(d.getOrDefault("expected_points", 5.0)).append(" xP** |\n");

            count++;
            if (count >= 5) break;
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderPlayerCompare(Map<String, Object> context, List<Player> resolvedPlayers, int nextGw) {
        List<Map<String, Object>> targets = (List<Map<String, Object>>) context.get("target_players");
        StringBuilder sb = new StringBuilder();
        sb.append("### PLAYER COMPARISON (GW ").append(nextGw).append(")\n\n");

        if (targets == null || targets.size() < 2) {
            sb.append("Please specify two players to compare (e.g. 'Compare Salah and Palmer').\n");
            return sb.toString();
        }

        sb.append("| Metric | ").append(targets.get(0).get("name")).append(" | ").append(targets.get(1).get("name")).append(" |\n");
        sb.append("|:---|:---:|:---:|\n");
        sb.append("| Position | ").append(targets.get(0).get("position")).append(" | ").append(targets.get(1).get("position")).append(" |\n");
        sb.append("| Price | £").append(targets.get(0).get("cost_m")).append("m | £").append(targets.get(1).get("cost_m")).append("m |\n");
        sb.append("| Form | ").append(targets.get(0).get("form")).append(" | ").append(targets.get(1).get("form")).append(" |\n");
        sb.append("| Total Points | ").append(targets.get(0).get("total_points")).append(" | ").append(targets.get(1).get("total_points")).append(" |\n");
        sb.append("| Minutes Played | ").append(targets.get(0).get("minutes")).append(" | ").append(targets.get(1).get("minutes")).append(" |\n");
        sb.append("| **Projected GW xP** | **").append(targets.get(0).get("xP_next")).append("** | **").append(targets.get(1).get("xP_next")).append("** |\n\n");

        double xp0 = ((Number) targets.get(0).getOrDefault("xP_next", 0.0)).doubleValue();
        double xp1 = ((Number) targets.get(1).getOrDefault("xP_next", 0.0)).doubleValue();

        if (xp0 > xp1) {
            sb.append("**Verdict**: **").append(targets.get(0).get("name"))
              .append("** holds higher projected output (+").append(String.format(Locale.US, "%.1f", xp0 - xp1))
              .append(" xP) over **").append(targets.get(1).get("name")).append("** for the upcoming gameweek.");
        } else if (xp1 > xp0) {
            sb.append("**Verdict**: **").append(targets.get(1).get("name"))
              .append("** holds higher projected output (+").append(String.format(Locale.US, "%.1f", xp1 - xp0))
              .append(" xP) over **").append(targets.get(0).get("name")).append("** for the upcoming gameweek.");
        } else {
            sb.append("**Verdict**: Both assets carry equivalent projected point yields. Select based on budget and fixture run.");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderPlayerInfo(Map<String, Object> context, List<Player> resolvedPlayers, int nextGw) {
        List<Map<String, Object>> targets = (List<Map<String, Object>>) context.get("target_players");
        StringBuilder sb = new StringBuilder();

        if (targets == null || targets.isEmpty()) {
            sb.append("Player not found in database. Please check the spelling.");
            return sb.toString();
        }

        Map<String, Object> p = targets.get(0);
        sb.append("### ").append(p.get("full_name")).append(" (").append(p.get("name")).append(")\n\n");
        sb.append("- **Position**: ").append(p.get("position")).append("\n");
        sb.append("- **Price**: £").append(p.get("cost_m")).append("m\n");
        sb.append("- **Form**: ").append(p.get("form")).append("\n");
        sb.append("- **Total Points**: ").append(p.get("total_points")).append(" (in ").append(p.get("minutes")).append(" mins)\n");
        sb.append("- **Projected GW ").append(nextGw).append(" xP**: **").append(p.get("xP_next")).append(" pts**\n");

        String news = String.valueOf(p.getOrDefault("news", ""));
        if (!news.isBlank()) {
            sb.append("- **Status News**: ⚠️ ").append(news).append("\n");
        } else {
            sb.append("- **Status**: Available (100% chance of playing)\n");
        }

        return sb.toString();
    }

    private String renderFixtureAnalysis(Map<String, Object> context, int nextGw) {
        return "### FIXTURE DIFFICULTY SCHEDULE (GW " + nextGw + "+)\n\n" +
               "FDR runs indicate favorable fixture ticker ratings for Arsenal, Manchester City, and Liverpool. " +
               "Target attackers with FDR ratings of 2 (favorable home ties) for maximum attacking ceiling.";
    }

    private String renderGeneralFpl(String query) {
        return "### FPL RULES & SCHEDULE\n\n" +
               "- **Deadlines**: The transfer deadline is strictly **90 minutes** prior to the first kickoff of each gameweek.\n" +
               "- **Transfers**: 1 free transfer per gameweek (can accumulate up to 5). Additional transfers deduct **-4 points** each.\n" +
               "- **Captains**: Captain scores **2x points**; Vice-Captain steps in if the captain plays 0 minutes.";
    }

    private String renderOffTopic() {
        return "I am your dedicated FPL Decision-Support Assistant! I can help you evaluate transfers, choose a captain, audit your squad weaknesses, compare players, and plan your chip strategy. What would you like to check for your squad?";
    }

    @SuppressWarnings("unchecked")
    private String extractPlayerName(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object val = map.get(k);
            if (val instanceof String s && !s.isBlank()) {
                return s;
            } else if (val instanceof Map<?, ?> m) {
                Object nameVal = m.get("web_name");
                if (nameVal != null) return String.valueOf(nameVal);
            }
        }
        return "Player";
    }

    private double extractDouble(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object val = map.get(k);
            if (val instanceof Number n) {
                return n.doubleValue();
            }
        }
        return 0.0;
    }
}
