package com.cloudops.asset.controller;

import com.cloudops.asset.dto.AssetRequest;
import com.cloudops.asset.dto.AssetResponse;
import com.cloudops.asset.dto.SshCredentialRequest;
import com.cloudops.asset.service.AssetService;
import com.cloudops.common.dto.ApiResponse;
import com.cloudops.common.security.AuthUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    public ApiResponse<AssetResponse> create(
            @Valid @RequestBody AssetRequest request,
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        return ApiResponse.ok(assetService.create(request, principal.getUserId(), principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_VIEWER')")
    public ApiResponse<List<AssetResponse>> list() {
        return ApiResponse.ok(assetService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_VIEWER')")
    public ApiResponse<AssetResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(assetService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    public ApiResponse<AssetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request,
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        return ApiResponse.ok(assetService.update(id, request, principal.getUserId(), principal.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        assetService.delete(id, principal.getUserId(), principal.getUsername());
        return ApiResponse.ok("资产已删除", null);
    }

    @PostMapping("/{id}/ssh-credential")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    public ApiResponse<Void> saveSshCredential(
            @PathVariable Long id,
            @Valid @RequestBody SshCredentialRequest request,
            @AuthenticationPrincipal AuthUserPrincipal principal) {
        assetService.saveSshCredential(id, request, principal.getUserId(), principal.getUsername());
        return ApiResponse.ok("SSH 凭证已保存", null);
    }
}
