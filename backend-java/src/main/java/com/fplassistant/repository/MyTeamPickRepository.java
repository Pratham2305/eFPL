package com.fplassistant.repository;

import com.fplassistant.model.MyTeamPick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyTeamPickRepository extends JpaRepository<MyTeamPick, Integer> {

    List<MyTeamPick> findByGameweekIdOrderByPositionAsc(Integer gameweekId);

    void deleteByGameweekId(Integer gameweekId);
}
