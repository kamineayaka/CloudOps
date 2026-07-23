package com.archops.terminal.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archops.asset.domain.SshAuthType;
import com.archops.asset.domain.SshCredential;
import com.archops.asset.service.AssetService;
import com.archops.common.config.SshPoolProperties;
import com.archops.common.exception.BusinessException;
import java.time.Duration;
import java.util.List;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetSshDialerTest {

    @Mock
    private AssetService assetService;
    @Mock
    private SshClient sshClient;
    @Mock
    private ClientSession session;

    private AssetSshDialer dialer;

    @BeforeEach
    void setUp() {
        SshPoolProperties properties = new SshPoolProperties();
        properties.setIdleTimeout(Duration.ofMinutes(5));
        properties.setConnectTimeout(Duration.ofSeconds(15));
        dialer = spy(new AssetSshDialer(assetService, sshClient, properties));
    }

    @Test
    void emptyJumpChainUsesDirectDial() throws Exception {
        SshCredential credential = new SshCredential();
        credential.setJumpAssetIds(List.of());
        when(assetService.getSshCredential(10L)).thenReturn(credential);
        doReturn(session).when(dialer).dialAssetDirect(10L);

        ClientSession result = dialer.dial(10L);

        assertThat(result).isSameAs(session);
        verify(dialer).dialAssetDirect(10L);
        verify(dialer, never()).dialViaJumpChain(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void nullJumpChainUsesDirectDial() throws Exception {
        SshCredential credential = new SshCredential();
        credential.setJumpAssetIds(null);
        when(assetService.getSshCredential(10L)).thenReturn(credential);
        doReturn(session).when(dialer).dialAssetDirect(10L);

        ClientSession result = dialer.dial(10L);

        assertThat(result).isSameAs(session);
        verify(dialer).dialAssetDirect(10L);
    }

    @Test
    void validateJumpChainDetectsCycleInJumps() {
        assertThatThrownBy(() -> AssetSshDialer.validateJumpChain(List.of(1L, 2L, 1L), 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("SSH_JUMP_CYCLE");
    }

    @Test
    void validateJumpChainDetectsTargetInJumpList() {
        assertThatThrownBy(() -> AssetSshDialer.validateJumpChain(List.of(2L, 3L), 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("SSH_JUMP_CYCLE");
    }

    @Test
    void validateJumpChainAllowsLinearPath() {
        AssetSshDialer.validateJumpChain(List.of(1L, 2L), 3L);
    }

    @Test
    void dialRejectsCyclicJumpChainBeforeConnecting() throws Exception {
        SshCredential credential = new SshCredential();
        credential.setAuthType(SshAuthType.PASSWORD);
        credential.setJumpAssetIds(List.of(10L, 20L));
        when(assetService.getSshCredential(10L)).thenReturn(credential);

        assertThatThrownBy(() -> dialer.dial(10L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("SSH_JUMP_CYCLE");

        verify(dialer, never()).dialAssetDirect(org.mockito.ArgumentMatchers.anyLong());
    }
}
