package com.archops.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archops.approval.domain.Approval;
import com.archops.approval.domain.ExecutionGrant;
import com.archops.approval.domain.RiskLevel;
import com.archops.approval.service.ApprovalGate;
import com.archops.approval.service.ApprovalService;
import com.archops.approval.service.ExecutionGrantService;
import com.archops.approval.service.RiskClassifier;
import com.archops.ai.llm.LlmProvider.ToolCall;
import com.archops.audit.service.AuditService;
import com.archops.tools.AgentTool;
import com.archops.tools.ToolRegistry;
import com.archops.user.domain.ApprovalPolicy;
import com.archops.user.domain.RbacTier;
import com.archops.user.domain.User;
import com.archops.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToolExecutorGrantTest {

    @Mock
    private ToolRegistry toolRegistry;
    @Mock
    private RiskClassifier riskClassifier;
    @Mock
    private ApprovalGate approvalGate;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private ExecutionGrantService executionGrantService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private AgentTool agentTool;

    private ToolExecutorService executor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        executor = new ToolExecutorService(
                toolRegistry,
                riskClassifier,
                approvalGate,
                approvalService,
                executionGrantService,
                userRepository,
                auditService,
                objectMapper);
    }

    @Test
    void secondCallUsesGrantAndSkipsApprovalGate() throws Exception {
        User user = user(10L, "ops");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(toolRegistry.find("ssh_exec")).thenReturn(Optional.of(agentTool));
        when(agentTool.execute(any(), any())).thenReturn("ok");
        when(riskClassifier.classify(eq("ssh_exec"), any())).thenReturn(RiskLevel.MEDIUM);

        ExecutionGrant grant = new ExecutionGrant();
        grant.setId(1L);
        when(executionGrantService.findMatching(
                        eq(10L), eq(42L), eq("ssh_exec"), eq(7L), eq(RiskLevel.MEDIUM), any()))
                .thenReturn(Optional.of(grant));

        ToolCall call = new ToolCall("tc1", "ssh_exec", "{\"assetId\":7,\"command\":\"df -h\"}");
        var ctx = new AgentTool.ExecutionContext(10L, "ops", 42L, List.of(7L), null);

        ToolExecutorService.ToolExecutionResult result = executor.execute(call, 10L, null, ctx);

        assertThat(result.status()).isEqualTo("SUCCESS");
        verify(approvalGate, never()).decide(any(), any(), any());
        verify(approvalService, never()).createPending(any(), any(), any(), any(), any());

        ArgumentCaptor<AuditService.AuditEntry> captor = ArgumentCaptor.forClass(AuditService.AuditEntry.class);
        verify(auditService).record(captor.capture());
        assertThat(captor.getValue().detail()).contains("\"decision_source\":\"GRANT\"");
    }

    @Test
    void proposeArchitectureUpdateNeverUsesGrantPath() throws Exception {
        User user = user(10L, "ops");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(toolRegistry.find("propose_architecture_update")).thenReturn(Optional.of(agentTool));
        when(agentTool.execute(any(), any())).thenReturn("{\"proposalId\":1}");
        when(riskClassifier.classify(eq("propose_architecture_update"), any())).thenReturn(RiskLevel.MEDIUM);
        when(executionGrantService.findMatching(any(), any(), eq("propose_architecture_update"), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(approvalGate.decide(any(), any(), eq(RiskLevel.MEDIUM)))
                .thenReturn(ApprovalGate.Decision.auto(RiskLevel.MEDIUM));

        ToolCall call = new ToolCall("tc1", "propose_architecture_update", "{}");
        var ctx = new AgentTool.ExecutionContext(10L, "ops", 42L, List.of(), null);

        ToolExecutorService.ToolExecutionResult result = executor.execute(call, 10L, null, ctx);

        assertThat(result.status()).isEqualTo("SUCCESS");
        ArgumentCaptor<AuditService.AuditEntry> captor = ArgumentCaptor.forClass(AuditService.AuditEntry.class);
        verify(auditService).record(captor.capture());
        assertThat(captor.getValue().detail()).contains("\"decision_source\":\"AUTO_POLICY\"");
        assertThat(captor.getValue().detail()).doesNotContain("\"decision_source\":\"GRANT\"");
    }

    @Test
    void crossUserGrantMissCreatesPendingApproval() {
        User user = user(99L, "other");
        when(userRepository.findById(99L)).thenReturn(Optional.of(user));
        when(toolRegistry.find("ssh_exec")).thenReturn(Optional.of(agentTool));
        when(riskClassifier.classify(eq("ssh_exec"), any())).thenReturn(RiskLevel.MEDIUM);
        when(executionGrantService.findMatching(
                        eq(99L), eq(42L), eq("ssh_exec"), eq(7L), eq(RiskLevel.MEDIUM), any()))
                .thenReturn(Optional.empty());
        when(approvalGate.decide(any(), any(), eq(RiskLevel.MEDIUM)))
                .thenReturn(ApprovalGate.Decision.requiresApproval(RiskLevel.MEDIUM));

        Approval pending = new Approval();
        pending.setId(55L);
        when(approvalService.createPending(any(), any(), any(), any(), any())).thenReturn(pending);

        ToolCall call = new ToolCall("tc1", "ssh_exec", "{\"assetId\":7,\"command\":\"df -h\"}");
        var ctx = new AgentTool.ExecutionContext(99L, "other", 42L, List.of(7L), null);

        ToolExecutorService.ToolExecutionResult result = executor.execute(call, 99L, null, ctx);

        assertThat(result.status()).isEqualTo("PENDING_APPROVAL");
        assertThat(result.approvalId()).isEqualTo(55L);
    }

    private static User user(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        user.setRbacTier(RbacTier.LOW);
        user.setApprovalPolicy(ApprovalPolicy.MANUAL_A);
        return user;
    }
}
