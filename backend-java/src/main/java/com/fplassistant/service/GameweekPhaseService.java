package com.fplassistant.service;

import com.fplassistant.dto.GameweekDto;
import com.fplassistant.model.Gameweek;
import com.fplassistant.repository.GameweekRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameweekPhaseService {

    private final GameweekRepository gameweekRepo;

    public GameweekPhaseService(GameweekRepository gameweekRepo) {
        this.gameweekRepo = gameweekRepo;
    }

    public String determinePhase(Gameweek gw) {
        if (gw == null) return "FUTURE";
        if (Boolean.TRUE.equals(gw.getFinished())) {
            return "FINISHED";
        }
        if (gw.getDeadlineTime() != null && gw.getDeadlineTime().isBefore(LocalDateTime.now())) {
            return "LIVE";
        }
        if (Boolean.TRUE.equals(gw.getIsNext())) {
            return "UPCOMING";
        }
        if (Boolean.TRUE.equals(gw.getIsCurrent())) {
            return "UPCOMING";
        }
        return "FUTURE";
    }

    public Integer getDefaultHomeGameweekId() {
        List<Gameweek> all = gameweekRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
        if (all.isEmpty()) return 1;

        // 1. Any LIVE gameweek takes precedence
        for (Gameweek gw : all) {
            if ("LIVE".equals(determinePhase(gw))) {
                return gw.getId();
            }
        }

        // 2. Current gameweek
        Gameweek current = all.stream().filter(gw -> Boolean.TRUE.equals(gw.getIsCurrent())).findFirst().orElse(null);
        if (current != null) {
            if ("FINISHED".equals(determinePhase(current))) {
                // Current is finished -> jump to UPCOMING (isNext)
                Gameweek next = all.stream().filter(gw -> Boolean.TRUE.equals(gw.getIsNext())).findFirst().orElse(null);
                if (next != null) {
                    return next.getId();
                }
                // fallback to current.getId() + 1 if within range
                int candidate = current.getId() + 1;
                if (all.stream().anyMatch(g -> g.getId().equals(candidate))) {
                    return candidate;
                }
                return current.getId();
            } else {
                return current.getId();
            }
        }

        // 3. Fallback to isNext
        Gameweek next = all.stream().filter(gw -> Boolean.TRUE.equals(gw.getIsNext())).findFirst().orElse(null);
        if (next != null) {
            return next.getId();
        }

        return all.get(0).getId();
    }

    public List<GameweekDto> getAllGameweeks() {
        List<Gameweek> all = gameweekRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
        Integer defaultHomeId = getDefaultHomeGameweekId();

        List<GameweekDto> result = new ArrayList<>();
        for (Gameweek gw : all) {
            String phase = determinePhase(gw);
            boolean isDefault = gw.getId().equals(defaultHomeId);

            result.add(GameweekDto.builder()
                    .id(gw.getId())
                    .name(gw.getName())
                    .deadlineTime(gw.getDeadlineTime())
                    .averageEntryScore(gw.getAverageEntryScore())
                    .highestScore(gw.getHighestScore())
                    .isCurrent(gw.getIsCurrent())
                    .isNext(gw.getIsNext())
                    .isPrevious(gw.getIsPrevious())
                    .finished(gw.getFinished())
                    .dataChecked(gw.getDataChecked())
                    .phase(phase)
                    .isDefaultHome(isDefault)
                    .build());
        }
        return result;
    }
}
