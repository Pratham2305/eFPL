package com.fplassistant.repository;

import com.fplassistant.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByTeamId(Integer teamId);

    List<Player> findByElementType(Short elementType);
}
