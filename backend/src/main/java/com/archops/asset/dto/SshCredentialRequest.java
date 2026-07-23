package com.archops.asset.dto;

import com.archops.asset.domain.SshAuthType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SshCredentialRequest(
        @NotBlank String username,
        @NotNull SshAuthType authType,
        @NotBlank String secret,
        List<Long> jumpAssetIds) {}
