package com.archops.user.dto;

import java.util.Set;

public record UserProfileResponse(
        Long id,
        String username,
        String displayName,
        String rbacTier,
        String approvalPolicy,
        Set<String> roles) {}
