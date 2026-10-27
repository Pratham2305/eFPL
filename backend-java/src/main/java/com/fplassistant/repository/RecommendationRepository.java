package com.fplassistant.repository;

import com.fplassistant.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {

    List<Recommendation> findByGameweekIdAndType(Integer gameweekId, String type);
}
