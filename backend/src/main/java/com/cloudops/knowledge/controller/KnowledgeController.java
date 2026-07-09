package com.cloudops.knowledge.controller;

import com.cloudops.common.dto.ApiResponse;
import com.cloudops.common.security.AuthUserPrincipal;
import com.cloudops.knowledge.domain.ArchitectureSnapshot;
import com.cloudops.knowledge.dto.ArchitectureRequest;
import com.cloudops.knowledge.dto.IndexStatsResponse;
import com.cloudops.knowledge.dto.ManualDocumentRequest;
import com.cloudops.knowledge.dto.ManualDocumentResponse;
import com.cloudops.knowledge.dto.RagChunkResponse;
import com.cloudops.knowledge.dto.RagSearchRequest;
import com.cloudops.knowledge.dto.ReindexResponse;
import com.cloudops.knowledge.dto.WorkLogResponse;
import com.cloudops.knowledge.service.KnowledgeRagService;
import com.cloudops.knowledge.service.KnowledgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeRagService knowledgeRagService;

    public KnowledgeController(KnowledgeService knowledgeService, KnowledgeRagService knowledgeRagService) {
        this.knowledgeService = knowledgeService;
        this.knowledgeRagService = knowledgeRagService;
    }

    @GetMapping("/architecture")
    public ApiResponse<ArchitectureSnapshot> latest() {
        return ApiResponse.ok(knowledgeService.latestArchitecture());
    }

    @PostMapping("/architecture")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ArchitectureSnapshot> update(
            @Valid @RequestBody ArchitectureRequest request,
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        return ApiResponse.ok(knowledgeService.updateArchitecture(
                request, principal.getUserId(), principal.getUsername()));
    }

    @GetMapping("/work-logs")
    public ApiResponse<List<WorkLogResponse>> workLogs() {
        return ApiResponse.ok(knowledgeService.recentLogs());
    }

    @PostMapping("/search")
    public ApiResponse<List<RagChunkResponse>> search(@Valid @RequestBody RagSearchRequest request) {
        return ApiResponse.ok(knowledgeRagService.search(request.query(), request.topK()));
    }

    @GetMapping("/index-stats")
    public ApiResponse<IndexStatsResponse> indexStats() {
        return ApiResponse.ok(knowledgeRagService.stats());
    }

    @PostMapping("/reindex")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ReindexResponse> reindex() {
        return ApiResponse.ok(knowledgeRagService.reindexAll());
    }

    @PostMapping("/documents")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ManualDocumentResponse> indexDocument(@Valid @RequestBody ManualDocumentRequest request) {
        return ApiResponse.ok(knowledgeRagService.indexManualDocument(request.title(), request.content()));
    }
}
