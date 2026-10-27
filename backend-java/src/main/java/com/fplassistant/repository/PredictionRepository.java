package com.fplassistant.repository;

import com.fplassistant.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Integer> {

    List<Prediction> findByPlayerIdAndGameweekId(Integer playerId, Integer gameweekId);

    List<Prediction> findByGameweekId(Integer gameweekId);

    List<Prediction> findByGameweekIdBetween(Integer startGw, Integer endGw);

    List<Prediction> findByPlayerIdAndGameweekIdBetween(Integer playerId, Integer startGw, Integer endGw);
}
