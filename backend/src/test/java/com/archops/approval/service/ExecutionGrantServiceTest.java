package com.archops.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archops.approval.ApprovalProperties;
import com.archops.approval.domain.Approval;
import com.archops.approval.domain.ExecutionGrant;
import com.archops.approval.domain.RiskLevel;
import com.archops.approval.repository.ExecutionGrantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionGrantServiceTest {

    @Mock
    private ExecutionGrantRepository grantRepository;

    private ApprovalProperties properties;
    private ExecutionGrantService grantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        properties = new ApprovalProperties();
        properties.setGrantTtlMinutes(60);
        properties.setAllowHighGrants(false);
        grantService = new ExecutionGrantService(grantRepository, properties, objectMapper);
    }

    @Test
    void createFromApprovalPersistsGrantForExecutionTool() {
        Approval approval = baseApproval(RiskLevel.MEDIUM);
        approval.setPayload("""
                {"tool":"ssh_exec","conversationId":42,"arguments":"{\\"assetId\\":7,\\"command\\":\\"df -h\\"}"}
                """);
        when(grantRepository.save(any(ExecutionGrant.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ExecutionGrant> created = grantService.createFromApproval(approval);

        assertThat(created).isPresent();
        ExecutionGrant grant = created.get();
        assertThat(grant.getUserId()).isEqualTo(10L);
        assertThat(grant.getConversationId()).isEqualTo(42L);
        assertThat(grant.getToolName()).isEqualTo("ssh_exec");
        assertThat(grant.getAssetId()).isEqualTo(7L);
        assertThat(grant.getPattern()).isEqualTo("df");
        assertThat(grant.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(grant.getCreatedByApprovalId()).isEqualTo(100L);
        assertThat(grant.getExpiresAt()).isAfter(Instant.now().plus(50, ChronoUnit.MINUTES));
    }

    @Test
    void highRiskCannotCreateGrantByDefault() {
        Approval approval = baseApproval(RiskLevel.HIGH);
        approval.setPayload("""
                {"tool":"ssh_exec","conversationId":42,"arguments":"{\\"command\\":\\"rm -rf /tmp/x\\"}"}
                """);

        Optional<ExecutionGrant> created = grantService.createFromApproval(approval);

        assertThat(created).isEmpty();
        verify(grantRepository, never()).save(any());
    }

    @Test
    void proposeArchitectureUpdateNeverCreatesGrant() {
        Approval approval = baseApproval(RiskLevel.MEDIUM);
        approval.setPayload("""
                {"tool":"propose_architecture_update","conversationId":42,"arguments":"{}"}
                """);

        assertThat(grantService.createFromApproval(approval)).isEmpty();
        verify(grantRepository, never()).save(any());
    }

    @Test
    void findMatchingHitsSameUserConversationTool() {
        ExecutionGrant grant = sampleGrant(10L, 42L, "ssh_exec", 7L, RiskLevel.LOW, "df");
        when(grantRepository.findActiveMatches(
                        eq(10L), eq(42L), eq("ssh_exec"), eq(RiskLevel.LOW), eq(7L), any(Instant.class)))
                .thenReturn(List.of(grant));

        Optional<ExecutionGrant> match = grantService.findMatching(
                10L, 42L, "ssh_exec", 7L, RiskLevel.LOW, "{\"command\":\"df -h\"}");

        assertThat(match).contains(grant);
    }

    @Test
    void findMatchingMissesCrossUser() {
        when(grantRepository.findActiveMatches(
                        eq(99L), eq(42L), eq("ssh_exec"), eq(RiskLevel.LOW), eq(7L), any(Instant.class)))
                .thenReturn(List.of());

        Optional<ExecutionGrant> match = grantService.findMatching(
                99L, 42L, "ssh_exec", 7L, RiskLevel.LOW, "{\"command\":\"df -h\"}");

        assertThat(match).isEmpty();
    }

    @Test
    void findMatchingRejectsProposeArchitectureUpdate() {
        Optional<ExecutionGrant> match = grantService.findMatching(
                10L, 42L, "propose_architecture_update", null, RiskLevel.MEDIUM, "{}");

        assertThat(match).isEmpty();
        verify(grantRepository, never()).findActiveMatches(any(), any(), any(), any(), any(), any());
        verify(grantRepository, never()).findActiveConversationWideMatches(any(), any(), any(), any(), any());
    }

    @Test
    void findMatchingRejectsHighUnlessConfigured() {
        Optional<ExecutionGrant> match = grantService.findMatching(
                10L, 42L, "ssh_exec", 7L, RiskLevel.HIGH, "{\"command\":\"rm -rf /tmp\"}");

        assertThat(match).isEmpty();
        verify(grantRepository, never()).findActiveMatches(any(), any(), any(), any(), any(), any());
    }

    @Test
    void highGrantAllowedWhenConfigured() {
        properties.setAllowHighGrants(true);
        ExecutionGrant grant = sampleGrant(10L, 42L, "ssh_exec", 7L, RiskLevel.HIGH, "rm");
        when(grantRepository.findActiveMatches(
                        eq(10L), eq(42L), eq("ssh_exec"), eq(RiskLevel.HIGH), eq(7L), any(Instant.class)))
                .thenReturn(List.of(grant));

        assertThat(grantService.findMatching(
                        10L, 42L, "ssh_exec", 7L, RiskLevel.HIGH, "{\"command\":\"rm -rf /tmp\"}"))
                .contains(grant);
    }

    private static Approval baseApproval(RiskLevel risk) {
        Approval approval = new Approval();
        approval.setId(100L);
        approval.setRequesterId(10L);
        approval.setRiskLevel(risk);
        return approval;
    }

    private static ExecutionGrant sampleGrant(
            Long userId, Long conversationId, String tool, Long assetId, RiskLevel risk, String pattern) {
        ExecutionGrant grant = new ExecutionGrant();
        grant.setId(1L);
        grant.setUserId(userId);
        grant.setConversationId(conversationId);
        grant.setToolName(tool);
        grant.setAssetId(assetId);
        grant.setRiskLevel(risk);
        grant.setPattern(pattern);
        grant.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        grant.setCreatedAt(Instant.now());
        return grant;
    }
}
