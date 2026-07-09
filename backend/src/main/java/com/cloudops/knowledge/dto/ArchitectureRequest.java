package com.cloudops.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record ArchitectureRequest(@NotBlank String content, String summary) {}
