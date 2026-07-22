package com.archops.user.service;

import com.archops.common.exception.BusinessException;
import com.archops.common.security.AuthUserPrincipal;
import com.archops.common.security.JwtTokenProvider;
import com.archops.user.domain.User;
import com.archops.user.dto.LoginRequest;
import com.archops.user.dto.LoginResponse;
import com.archops.user.dto.RefreshTokenRequest;
import com.archops.user.dto.UserProfileResponse;
import com.archops.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            SessionService sessionService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AuthUserPrincipal principal = (AuthUserPrincipal) authentication.getPrincipal();

        String sessionId = sessionService.createSession(principal.getUserId());
        String accessToken = jwtTokenProvider.createAccessToken(
                principal.getUserId(), principal.getUsername(), sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(
                principal.getUserId(), principal.getUsername(), sessionId);

        User user = userRepository
                .findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs() / 1000,
                toProfile(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        Claims claims = jwtTokenProvider.parseClaims(request.refreshToken());
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "无效的刷新令牌");
        }

        Long userId = Long.valueOf(claims.getSubject());
        String sessionId = claims.get("sessionId", String.class);
        if (!sessionService.isSessionValid(userId, sessionId)) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED, "SESSION_KICKED", "账号已在其他设备登录，请重新登录");
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));

        String accessToken = jwtTokenProvider.createAccessToken(userId, user.getUsername(), sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, user.getUsername(), sessionId);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs() / 1000,
                toProfile(user));
    }

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUserPrincipal principal) {
            sessionService.invalidateSession(principal.getUserId());
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentUser() {
        AuthUserPrincipal principal = currentPrincipal();
        User user = userRepository
                .findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
        return toProfile(user);
    }

    public AuthUserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "未登录");
        }
        return principal;
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRbacTier().name(),
                user.getApprovalPolicy().name(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()));
    }
}
