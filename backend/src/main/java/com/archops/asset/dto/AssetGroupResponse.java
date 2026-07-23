package com.archops.asset.dto;

import java.time.Instant;
import java.util.List;

public record AssetGroupResponse(
        Long id,
        String name,
        String description,
        boolean enabled,
        int memberCount,
        List<AssetMemberSummary> members,
        Instant createdAt,
        Instant updatedAt) {

    public record AssetMemberSummary(Long id, String name, String kind, String host) {}
}
