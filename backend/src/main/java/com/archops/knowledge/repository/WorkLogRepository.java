package com.archops.knowledge.repository;

import com.archops.knowledge.domain.WorkLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findTop20ByOrderByCreatedAtDesc();
}
