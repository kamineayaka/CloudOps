package com.archops.asset.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssetGroupMembersRequest(@NotNull List<Long> assetIds) {}
