package com.fplassistant.repository;

import com.fplassistant.model.MyTeamState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyTeamStateRepository extends JpaRepository<MyTeamState, Integer> {

    Optional<MyTeamState> findByGameweekId(Integer gameweekId);

    Optional<MyTeamState> findTopByOrderByGameweekIdDesc();
}
