package com.archops.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archops.credentials")
public record CredentialProperties(String masterKey) {}
