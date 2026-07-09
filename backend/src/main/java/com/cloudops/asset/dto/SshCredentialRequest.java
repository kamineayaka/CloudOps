package com.cloudops.asset.dto;

import com.cloudops.asset.domain.SshAuthType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SshCredentialRequest(
        @NotBlank String username,
        @NotNull SshAuthType authType,
        @NotBlank String secret) {}
