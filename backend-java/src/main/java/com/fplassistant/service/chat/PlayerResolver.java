package com.fplassistant.service.chat;

import com.fplassistant.model.Player;
import com.fplassistant.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlayerResolver {

    private static final Logger log = LoggerFactory.getLogger(PlayerResolver.class);
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");

    private final PlayerRepository playerRepository;

    private static final Map<String, String> COMMON_ALIASES = new LinkedHashMap<>();
    static {
        COMMON_ALIASES.put("mo salah", "salah");
        COMMON_ALIASES.put("mohamed salah", "salah");
        COMMON_ALIASES.put("trent alexander-arnold", "alexander-arnold");
        COMMON_ALIASES.put("trent", "alexander-arnold");
        COMMON_ALIASES.put("taa", "alexander-arnold");
        COMMON_ALIASES.put("kevin de bruyne", "de bruyne");
        COMMON_ALIASES.put("de bruyne", "de bruyne");
        COMMON_ALIASES.put("kdb", "de bruyne");
        COMMON_ALIASES.put("erling haaland", "haaland");
        COMMON_ALIASES.put("erling", "haaland");
        COMMON_ALIASES.put("cole palmer", "palmer");
        COMMON_ALIASES.put("bukayo saka", "saka");
        COMMON_ALIASES.put("ollie watkins", "watkins");
        COMMON_ALIASES.put("heung-min son", "son");
        COMMON_ALIASES.put("sonny", "son");
        COMMON_ALIASES.put("luis diaz", "diaz");
        COMMON_ALIASES.put("lucho", "diaz");
        COMMON_ALIASES.put("bryan mbeumo", "mbeumo");
        COMMON_ALIASES.put("antoine semenyo", "semenyo");
        COMMON_ALIASES.put("morgan rogers", "rogers");
        COMMON_ALIASES.put("chris wood", "wood");
        COMMON_ALIASES.put("josko gvardiol", "gvardiol");
        COMMON_ALIASES.put("pedro porro", "porro");
        COMMON_ALIASES.put("william saliba", "saliba");
        COMMON_ALIASES.put("gabriel magalhaes", "gabriel");
        COMMON_ALIASES.put("david raya", "raya");
        COMMON_ALIASES.put("dominic solanke", "solanke");
        COMMON_ALIASES.put("alexander isak", "isak");
        COMMON_ALIASES.put("jarrod bowen", "bowen");
        COMMON_ALIASES.put("eberechi eze", "eze");
        COMMON_ALIASES.put("mohammed kudus", "kudus");
        COMMON_ALIASES.put("bruno fernandes", "fernandes");
        COMMON_ALIASES.put("bruno", "fernandes");
        COMMON_ALIASES.put("marcus rashford", "rashford");
        COMMON_ALIASES.put("nicolas jackson", "jackson");
        COMMON_ALIASES.put("noni madueke", "madueke");
        COMMON_ALIASES.put("kai havertz", "havertz");
        COMMON_ALIASES.put("virgil van dijk", "van dijk");
        COMMON_ALIASES.put("van dijk", "van dijk");
        COMMON_ALIASES.put("vvd", "van dijk");
        COMMON_ALIASES.put("lucas digne", "digne");
        COMMON_ALIASES.put("andy robertson", "robertson");
        COMMON_ALIASES.put("robbo", "robertson");
        COMMON_ALIASES.put("jordan pickford", "pickford");
        COMMON_ALIASES.put("phil foden", "foden");
    }

    public PlayerResolver(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public static String normalize(String input) {
        if (input == null) return "";
        String s = input.replace("Ø", "O").replace("ø", "o")
                .replace("Æ", "Ae").replace("æ", "ae")
                .replace("ß", "ss")
                .replace("Đ", "D").replace("đ", "d")
                .replace("Ł", "L").replace("ł", "l");
        String decomposed = Normalizer.normalize(s, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(decomposed).replaceAll("");
        return withoutDiacritics.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record MatchCandidate(Player player, int start, int end, int score) {}

    /**
     * Resolves players mentioned in a user query, avoiding multiple player assignments
     * to the exact same text span, and falling back to pronoun resolution.
     */
    public List<Player> resolvePlayers(String query, List<String> recentEntities) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String normalizedQuery = normalize(query);
        List<Player> allPlayers = playerRepository.findAll();
        if (allPlayers.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchCandidate> candidates = new ArrayList<>();

        // 1. Check explicit alias phrases
        for (Map.Entry<String, String> alias : COMMON_ALIASES.entrySet()) {
            String aliasKey = alias.getKey();
            Pattern p = Pattern.compile("\\b" + Pattern.quote(aliasKey) + "\\b");
            Matcher m = p.matcher(normalizedQuery);
            while (m.find()) {
                String targetWebName = alias.getValue();
                Optional<Player> matched = findPlayerByWebName(allPlayers, targetWebName);
                if (matched.isPresent()) {
                    Player pl = matched.get();
                    int score = 1000 + (pl.getTotalPoints() != null ? pl.getTotalPoints() : 0);
                    candidates.add(new MatchCandidate(pl, m.start(), m.end(), score));
                }
            }
        }

        // 2. Direct player webName, secondName, and fullName matching
        for (Player pl : allPlayers) {
            String webName = normalize(pl.getWebName());
            String secondName = normalize(pl.getSecondName());
            String fullName = normalize(pl.getFirstName() + " " + pl.getSecondName());

            int baseScore = (pl.getTotalPoints() != null ? pl.getTotalPoints() : 0)
                    + (pl.getNowCost() != null ? pl.getNowCost() : 0);

            // Full name
            if (fullName.length() >= 5) {
                findSpans(normalizedQuery, fullName).forEach(span ->
                        candidates.add(new MatchCandidate(pl, span[0], span[1], 500 + baseScore)));
            }

            // Web name
            if (webName.length() >= 3) {
                findSpans(normalizedQuery, webName).forEach(span ->
                        candidates.add(new MatchCandidate(pl, span[0], span[1], 300 + baseScore)));
            }

            // Second name
            if (secondName.length() >= 4 && !secondName.equals(webName)) {
                findSpans(normalizedQuery, secondName).forEach(span ->
                        candidates.add(new MatchCandidate(pl, span[0], span[1], 100 + baseScore)));
            }
        }

        // 3. Resolve non-overlapping spans with highest score first
        candidates.sort((a, b) -> Integer.compare(b.score(), a.score()));

        List<MatchCandidate> selected = new ArrayList<>();
        for (MatchCandidate cand : candidates) {
            boolean overlaps = false;
            for (MatchCandidate sel : selected) {
                if (cand.start() < sel.end() && cand.end() > sel.start()) {
                    overlaps = true;
                    break;
                }
                if (cand.player().getId().equals(sel.player().getId())) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                selected.add(cand);
            }
        }

        // Sort selected by appearance order in query
        selected.sort(Comparator.comparingInt(MatchCandidate::start));

        List<Player> resolved = new ArrayList<>();
        for (MatchCandidate sel : selected) {
            resolved.add(sel.player());
        }

        // 4. Pronoun resolution if no players found
        boolean hasPronoun = normalizedQuery.matches(".*\\b(he|him|his|they|them|instead|why not him|what about him)\\b.*");
        if (resolved.isEmpty() && hasPronoun && recentEntities != null && !recentEntities.isEmpty()) {
            for (String entity : recentEntities) {
                String normEntity = normalize(entity);
                for (Player p : allPlayers) {
                    String webName = normalize(p.getWebName());
                    String secondName = normalize(p.getSecondName());
                    if (normEntity.contains(webName) || normEntity.contains(secondName)) {
                        resolved.add(p);
                        break;
                    }
                }
                if (!resolved.isEmpty()) break;
            }
        }

        return resolved;
    }

    private List<int[]> findSpans(String text, String phrase) {
        List<int[]> spans = new ArrayList<>();
        if (phrase == null || phrase.isBlank()) return spans;
        Pattern p = Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b");
        Matcher m = p.matcher(text);
        while (m.find()) {
            spans.add(new int[]{m.start(), m.end()});
        }
        return spans;
    }

    private Optional<Player> findPlayerByWebName(List<Player> players, String targetWebName) {
        String normTarget = normalize(targetWebName);
        return players.stream()
                .filter(p -> normalize(p.getWebName()).equalsIgnoreCase(normTarget) ||
                             normalize(p.getSecondName()).equalsIgnoreCase(normTarget))
                .max(Comparator.comparingInt(p -> p.getTotalPoints() != null ? p.getTotalPoints() : 0));
    }
}
