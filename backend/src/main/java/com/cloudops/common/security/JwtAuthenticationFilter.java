package com.cloudops.common.security;

import com.cloudops.common.exception.BusinessException;
import com.cloudops.user.service.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService,
            SessionService sessionService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if (!jwtTokenProvider.isAccessToken(claims)) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "无效的访问令牌");
            }

            Long userId = Long.valueOf(claims.getSubject());
            String sessionId = claims.get("sessionId", String.class);
            if (!sessionService.isSessionValid(userId, sessionId)) {
                throw new BusinessException(
                        HttpStatus.UNAUTHORIZED, "SESSION_KICKED", "账号已在其他设备登录，请重新登录");
            }

            String username = claims.get("username", String.class);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                AuthUserPrincipal principal = new AuthUserPrincipal(
                        userId,
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        sessionId,
                        userDetails.isEnabled(),
                        userDetails.getAuthorities().stream()
                                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                                .collect(Collectors.toSet()));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BusinessException ex) {
            SecurityContextHolder.clearContext();
            request.setAttribute("auth_error_code", ex.getCode());
            request.setAttribute("auth_error_message", ex.getMessage());
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            request.setAttribute("auth_error_code", "INVALID_TOKEN");
            request.setAttribute("auth_error_message", "无效的访问令牌");
        }

        filterChain.doFilter(request, response);
    }
}
