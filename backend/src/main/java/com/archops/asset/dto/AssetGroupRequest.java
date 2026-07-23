package com.archops.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetGroupRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 512) String description,
        Boolean enabled) {}
