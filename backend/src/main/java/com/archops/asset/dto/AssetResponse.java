package com.archops.asset.dto;

import com.archops.asset.domain.AssetKind;
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
