package com.archops.terminal.pool;

/**
 * Pool entries are scoped per ArchOps user and asset so sessions are not shared
 * across operators even when targeting the same machine.
 */
public record SshPoolKey(Long userId, Long assetId) {}
