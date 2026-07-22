package com.archops.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archops.cors")
public record CorsProperties(List<String> allowedOrigins) {}
