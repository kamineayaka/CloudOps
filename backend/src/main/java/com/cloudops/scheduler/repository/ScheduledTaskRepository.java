package com.cloudops.scheduler.repository;

import com.cloudops.scheduler.domain.ScheduledTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    List<ScheduledTask> findByEnabledTrue();
}
