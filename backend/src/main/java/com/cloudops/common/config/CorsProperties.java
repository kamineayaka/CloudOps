package com.cloudops.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudops.cors")
public record CorsProperties(List<String> allowedOrigins) {}
