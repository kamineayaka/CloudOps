package com.archops.approval.service;

import com.archops.approval.domain.RiskLevel;
import com.archops.user.domain.ApprovalPolicy;
import com.archops.user.domain.RbacTier;
import org.springframework.stereotype.Service;

/**
 * RBAC-tiered approval policy gate.
 * LOW tier: only policy A (all manual).
 * MID tier: policy A or B.
 * HIGH tier: policy A, B, or C.
 */
@Service
public class ApprovalGate {

    public Decision decide(RbacTier tier, ApprovalPolicy policy, RiskLevel risk) {
        ApprovalPolicy effective = normalizePolicy(tier, policy);
        return switch (effective) {
            case MANUAL_A -> Decision.requiresApproval(risk);
            case RISK_BASED_B -> risk == RiskLevel.LOW ? Decision.auto(risk) : Decision.requiresApproval(risk);
            case AUTO_C -> risk == RiskLevel.HIGH ? Decision.requiresApproval(risk) : Decision.auto(risk);
        };
    }

    private ApprovalPolicy normalizePolicy(RbacTier tier, ApprovalPolicy policy) {
        return switch (tier) {
            case LOW -> ApprovalPolicy.MANUAL_A;
            case MID -> policy == ApprovalPolicy.AUTO_C ? ApprovalPolicy.RISK_BASED_B : policy;
            case HIGH -> policy;
        };
    }

    public record Decision(boolean autoExecute, RiskLevel riskLevel, ApprovalPolicy effectivePolicy) {
        public static Decision auto(RiskLevel risk) {
            return new Decision(true, risk, null);
        }

        public static Decision requiresApproval(RiskLevel risk) {
            return new Decision(false, risk, null);
        }
    }
}
