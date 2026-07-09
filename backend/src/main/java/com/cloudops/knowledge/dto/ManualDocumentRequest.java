package com.cloudops.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record ManualDocumentRequest(
        @NotBlank String title,
        @NotBlank String content) {}
