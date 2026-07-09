package com.cloudops.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudops.credentials")
public record CredentialProperties(String masterKey) {}
