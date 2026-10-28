package com.fplassistant.repository;

import com.fplassistant.model.MyTeamPick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyTeamPickRepository extends JpaRepository<MyTeamPick, Integer> {

    List<MyTeamPick> findByGameweekIdOrderByPositionAsc(Integer gameweekId);

    Optional<MyTeamPick> findByGameweekIdAndPlayerId(Integer gameweekId, Integer playerId);

    long countByGameweekId(Integer gameweekId);

    void deleteByGameweekId(Integer gameweekId);
}
