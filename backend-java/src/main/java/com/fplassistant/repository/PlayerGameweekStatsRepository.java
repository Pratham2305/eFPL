package com.fplassistant.repository;

import com.fplassistant.model.PlayerGameweekStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerGameweekStatsRepository extends JpaRepository<PlayerGameweekStats, Integer> {

    List<PlayerGameweekStats> findByPlayerId(Integer playerId);

    List<PlayerGameweekStats> findByPlayerIdOrderByGameweekIdDesc(Integer playerId);

    List<PlayerGameweekStats> findByGameweekId(Integer gameweekId);

    List<PlayerGameweekStats> findByPlayerIdAndGameweekId(Integer playerId, Integer gameweekId);
}
