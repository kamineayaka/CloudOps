package com.cloudops.knowledge.repository;

import com.cloudops.knowledge.domain.ArchitectureSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchitectureSnapshotRepository extends JpaRepository<ArchitectureSnapshot, Long> {
    Optional<ArchitectureSnapshot> findTopByOrderByVersionDesc();
}
