package com.cloudops.knowledge.dto;

public record ReindexResponse(int totalChunks, int sourcesProcessed, String status) {}
