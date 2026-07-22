package com.archops.terminal.pool;

import com.archops.common.config.SshPoolProperties;
import org.apache.sshd.client.SshClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(SshPoolProperties.class)
public class SshPoolConfig {

    @Bean(destroyMethod = "stop")
    public SshClient sshClient() {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        return client;
    }
}
