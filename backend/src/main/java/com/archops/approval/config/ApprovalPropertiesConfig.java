package com.archops.approval.config;

import com.archops.approval.ApprovalProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApprovalProperties.class)
public class ApprovalPropertiesConfig {}
