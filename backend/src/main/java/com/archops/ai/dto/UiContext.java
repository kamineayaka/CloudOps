package com.archops.ai.dto;

import java.util.List;

/**
 * Optional client-reported UI hint for prompt assembly.
 * Authoritative targets and ACL remain server-side; this is advisory only.
 */
public record UiContext(
        String route,
        String surface,
        Long selectedAssetId,
        List<Long> selectedAssetIds) {}
