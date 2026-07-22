package com.archops.asset.dto;

import com.archops.asset.domain.AssetKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
        @NotBlank String name,
        @NotNull AssetKind kind,
        String host,
        Integer port,
        String metadata,
        Long parentId,
        Boolean enabled) {}
