package com.fplassistant.service;

import com.fplassistant.dto.FixtureDto;
import com.fplassistant.model.Fixture;
import com.fplassistant.repository.FixtureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieves fixture data from the local database.
 */
@Service
public class FixtureService {

    private final FixtureRepository fixtureRepo;

    public FixtureService(FixtureRepository fixtureRepo) {
        this.fixtureRepo = fixtureRepo;
    }

    @Transactional(readOnly = true)
    public List<FixtureDto> getFixtures(Integer gameweekId, Integer teamId, Integer rangeEnd) {
        List<Fixture> fixtures;

        if (teamId != null) {
            fixtures = fixtureRepo.findByTeamHomeIdOrTeamAwayId(teamId, teamId);
        } else if (gameweekId != null && rangeEnd != null) {
            fixtures = fixtureRepo.findByGameweekIdBetween(gameweekId, rangeEnd);
        } else if (gameweekId != null) {
            fixtures = fixtureRepo.findByGameweekId(gameweekId);
        } else {
            fixtures = fixtureRepo.findAll();
        }

        return fixtures.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FixtureDto toDto(Fixture f) {
        return FixtureDto.builder()
                .id(f.getId())
                .gameweekId(f.getGameweek() != null ? f.getGameweek().getId() : null)
                .homeTeamName(f.getTeamHome().getName())
                .homeTeamShortName(f.getTeamHome().getShortName())
                .awayTeamName(f.getTeamAway().getName())
                .awayTeamShortName(f.getTeamAway().getShortName())
                .homeDifficulty(f.getTeamHomeDifficulty())
                .awayDifficulty(f.getTeamAwayDifficulty())
                .kickoffTime(f.getKickoffTime())
                .finished(f.getFinished())
                .homeScore(f.getTeamHomeScore())
                .awayScore(f.getTeamAwayScore())
                .build();
    }
}
