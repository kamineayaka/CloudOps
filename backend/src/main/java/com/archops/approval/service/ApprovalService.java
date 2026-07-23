package com.archops.approval.service;

import com.archops.approval.domain.Approval;
import com.archops.approval.domain.ApprovalStatus;
import com.archops.approval.domain.RiskLevel;
import com.archops.approval.dto.ApprovalResponse;
import com.archops.approval.repository.ApprovalRepository;
import com.archops.audit.service.AuditService;
import com.archops.common.exception.BusinessException;
import com.archops.user.domain.User;
import com.archops.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ExecutionGrantService executionGrantService;
    private final ObjectMapper objectMapper;

    public ApprovalService(
            ApprovalRepository approvalRepository,
            UserRepository userRepository,
            AuditService auditService,
            ExecutionGrantService executionGrantService,
            ObjectMapper objectMapper) {
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.executionGrantService = executionGrantService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Approval createPending(
            Long requesterId,
            String action,
            String resource,
            RiskLevel riskLevel,
            Map<String, Object> payload) {
        Approval approval = new Approval();
        approval.setRequesterId(requesterId);
        approval.setAction(action);
        approval.setResource(resource);
        approval.setRiskLevel(riskLevel);
        approval.setPayload(writeJson(payload));
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setCreatedAt(Instant.now());
        return approvalRepository.save(approval);
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> listPending() {
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> listMine(Long userId) {
        return approvalRepository.findByRequesterIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApprovalResponse decide(Long approvalId, Long approverId, String decision, String reason) {
        return decide(approvalId, approverId, decision, reason, false);
    }

    @Transactional
    public ApprovalResponse decide(
            Long approvalId,
            Long approverId,
            String decision,
            String reason,
            boolean rememberForSession) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "APPROVAL_NOT_FOUND", "审批单不存在"));
        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(HttpStatus.CONFLICT, "APPROVAL_ALREADY_DECIDED", "审批单已处理");
        }
        boolean approved = "APPROVE".equalsIgnoreCase(decision);
        approval.setApproverId(approverId);
        approval.setStatus(approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        approval.setReason(reason);
        approval.setDecidedAt(Instant.now());
        approvalRepository.save(approval);

        if (approved && rememberForSession) {
            executionGrantService.createFromApproval(approval);
        }

        User approver = userRepository.findById(approverId).orElse(null);
        auditService.record(new AuditService.AuditEntry(
                approverId,
                approver != null ? approver.getUsername() : null,
                approved ? "approval.approve" : "approval.reject",
                "approval:" + approvalId,
                approval.getRiskLevel().name(),
                "SUCCESS",
                approval.getPayload(),
                null,
                null));
        return toResponse(approval);
    }

    @Transactional(readOnly = true)
    public Approval getRequired(Long id) {
        return approvalRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "APPROVAL_NOT_FOUND", "审批单不存在"));
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD", "审批载荷无效");
        }
    }

    private ApprovalResponse toResponse(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getRequesterId(),
                approval.getApproverId(),
                approval.getAction(),
                approval.getResource(),
                approval.getRiskLevel(),
                approval.getPayload(),
                approval.getStatus(),
                approval.getReason(),
                approval.getCreatedAt(),
                approval.getDecidedAt());
    }
}
