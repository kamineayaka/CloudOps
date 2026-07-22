package com.archops.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record RagSearchRequest(
        @NotBlank String query,
        Integer topK) {}
