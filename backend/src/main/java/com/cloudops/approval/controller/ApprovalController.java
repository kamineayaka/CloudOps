package com.cloudops.approval.controller;

import com.cloudops.approval.dto.ApprovalDecisionRequest;
import com.cloudops.approval.dto.ApprovalResponse;
import com.cloudops.approval.service.ApprovalService;
import com.cloudops.common.dto.ApiResponse;
import com.cloudops.common.security.AuthUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    public ApiResponse<List<ApprovalResponse>> pending() {
        return ApiResponse.ok(approvalService.listPending());
    }

    @GetMapping("/mine")
    public ApiResponse<List<ApprovalResponse>> mine(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ApiResponse.ok(approvalService.listMine(principal.getUserId()));
    }

    @PostMapping("/{id}/decide")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    public ApiResponse<ApprovalResponse> decide(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecisionRequest request,
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        return ApiResponse.ok(approvalService.decide(id, principal.getUserId(), request.decision(), request.reason()));
    }
}
