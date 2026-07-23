package com.archops.approval.domain;

/**
 * Why a tool execution was allowed or denied (OpsKat-aligned audit field).
 */
public enum DecisionSource {
    /** Auto-allowed by RBAC / ApprovalGate policy. */
    AUTO_POLICY,
    /** Explicit human approval for this invocation. */
    USER_APPROVAL,
    /** Matched a session execution grant created from a prior approval. */
    GRANT,
    /** Denied (policy or rejected approval). */
    DENY
}
