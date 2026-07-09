package com.cloudops.approval.dto;

import com.cloudops.approval.domain.ApprovalStatus;
import com.cloudops.approval.domain.RiskLevel;
import java.time.Instant;

public record ApprovalResponse(
        Long id,
        Long requesterId,
        Long approverId,
        String action,
        String resource,
        RiskLevel riskLevel,
        String payload,
        ApprovalStatus status,
        String reason,
        Instant createdAt,
        Instant decidedAt) {}
