package com.cloudops.user.dto;

import com.cloudops.user.domain.ApprovalPolicy;
import com.cloudops.user.domain.RbacTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 6, max = 128) String password) {}
