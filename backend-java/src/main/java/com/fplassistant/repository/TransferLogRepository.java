package com.fplassistant.repository;

import com.fplassistant.model.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferLogRepository extends JpaRepository<TransferLog, Integer> {

    List<TransferLog> findByGameweekId(Integer gameweekId);

    List<TransferLog> findByGameweekIdOrderByTransferTimeDesc(Integer gameweekId);

    List<TransferLog> findAllByOrderByGameweekIdAscTransferTimeAsc();

    boolean existsByGameweekIdAndPlayerInIdAndPlayerOutIdAndTransferTime(
            Integer gameweekId, Integer playerInId, Integer playerOutId, LocalDateTime transferTime);
}
