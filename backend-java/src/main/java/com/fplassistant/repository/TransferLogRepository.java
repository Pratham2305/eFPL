package com.fplassistant.repository;

import com.fplassistant.model.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferLogRepository extends JpaRepository<TransferLog, Integer> {

    List<TransferLog> findByGameweekId(Integer gameweekId);
}
