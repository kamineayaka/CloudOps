package com.cloudops.asset.dto;

import com.cloudops.asset.domain.AssetKind;
import java.time.Instant;

public record AssetResponse(
        Long id,
        String name,
        AssetKind kind,
        String host,
        Integer port,
        String metadata,
        Long parentId,
        boolean enabled,
        boolean hasSshCredential,
        Instant createdAt,
        Instant updatedAt) {}
