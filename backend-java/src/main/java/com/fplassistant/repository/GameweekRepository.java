package com.fplassistant.repository;

import com.fplassistant.model.Gameweek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameweekRepository extends JpaRepository<Gameweek, Integer> {

    Optional<Gameweek> findByIsCurrentTrue();

    Optional<Gameweek> findByIsNextTrue();
}
