package com.archops.approval.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalDecisionRequest(
        @NotBlank String decision,
        String reason,
        Boolean rememberForSession) {}
