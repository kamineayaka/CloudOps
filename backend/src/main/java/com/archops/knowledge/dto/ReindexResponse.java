package com.archops.knowledge.dto;

public record ReindexResponse(int totalChunks, int sourcesProcessed, String status) {}
