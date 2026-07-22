package com.archops.audit.controller;

import com.archops.audit.domain.AuditLog;
import com.archops.audit.service.AuditService;
import com.archops.common.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Page<AuditLog>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(auditService.list(PageRequest.of(page, size)));
    }

    @GetMapping("/verify")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Boolean> verifyChain() {
        return ApiResponse.ok("审计链完整性校验", auditService.verifyChain());
    }
}
