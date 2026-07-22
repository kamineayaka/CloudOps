package com.archops.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archops.secrets")
public record SecretStoreProperties(String path) {}
