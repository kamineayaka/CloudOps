package com.archops.tools;

import com.archops.common.exception.BusinessException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;

/** Helpers for restricting agent tools to conversation target asset unions. */
public final class ToolScope {

    private ToolScope() {}

    public static void assertInScope(List<Long> allowedTargetAssetIds, Long assetId) {
        if (allowedTargetAssetIds == null || allowedTargetAssetIds.isEmpty()) {
            return;
        }
        if (assetId == null || !allowedTargetAssetIds.contains(assetId)) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "TOOL_OUT_OF_SCOPE",
                    "资产 " + assetId + " 不在当前对话目标范围内");
        }
    }

    public static Set<Long> allowedSet(List<Long> allowedTargetAssetIds) {
        if (allowedTargetAssetIds == null || allowedTargetAssetIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(allowedTargetAssetIds);
    }
}
