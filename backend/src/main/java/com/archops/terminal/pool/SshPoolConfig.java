package com.archops.terminal.pool;

import com.archops.common.config.SshPoolProperties;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
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
        // Required for jump-host local port forwarding (DirectTcpip tunnels).
        client.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
        client.start();
        return client;
    }
}
