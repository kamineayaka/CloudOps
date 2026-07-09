package com.cloudops.common.security;

import com.cloudops.user.service.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Authenticates WebSocket connections via JWT token query parameter.
 * Browsers cannot set Authorization headers on WebSocket handshakes,
 * so clients pass {@code ?token=<accessToken>} instead.
 */
@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;

    public WebSocketAuthHandshakeInterceptor(JwtTokenProvider jwtTokenProvider, SessionService sessionService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionService = sessionService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest http = servletRequest.getServletRequest();
        String token = http.getParameter("token");
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if (!jwtTokenProvider.isAccessToken(claims)) {
                return false;
            }
            Long userId = Long.valueOf(claims.getSubject());
            String sessionId = claims.get("sessionId", String.class);
            if (!sessionService.isSessionValid(userId, sessionId)) {
                return false;
            }
            String username = claims.get("username", String.class);
            AuthUserPrincipal principal = new AuthUserPrincipal(
                    userId, username, "", sessionId, true, java.util.Set.of("USER"));
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            principal, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            attributes.put("userId", userId);
            attributes.put("username", username);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // no-op
    }
}
