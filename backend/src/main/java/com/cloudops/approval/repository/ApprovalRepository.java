package com.cloudops.approval.repository;

import com.cloudops.approval.domain.Approval;
import com.cloudops.approval.domain.ApprovalStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
    List<Approval> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
}
