package com.archops.approval.repository;

import com.archops.approval.domain.Approval;
import com.archops.approval.domain.ApprovalStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
    List<Approval> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
}
