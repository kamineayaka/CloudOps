package com.cloudops.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudops.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs) {}
