package com.fplassistant.repository;

import com.fplassistant.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixtureRepository extends JpaRepository<Fixture, Integer> {

    List<Fixture> findByGameweekId(Integer gameweekId);

    List<Fixture> findByGameweekIdBetween(Integer startGw, Integer endGw);

    List<Fixture> findByTeamHomeIdOrTeamAwayId(Integer teamHomeId, Integer teamAwayId);
}
