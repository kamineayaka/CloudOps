package com.cloudops.approval.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalDecisionRequest(@NotBlank String decision, String reason) {}
