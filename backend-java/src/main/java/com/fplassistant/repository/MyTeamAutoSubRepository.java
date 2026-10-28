package com.fplassistant.repository;

import com.fplassistant.model.MyTeamAutoSub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyTeamAutoSubRepository extends JpaRepository<MyTeamAutoSub, Integer> {

    List<MyTeamAutoSub> findByGameweekId(Integer gameweekId);

    void deleteByGameweekId(Integer gameweekId);
}
