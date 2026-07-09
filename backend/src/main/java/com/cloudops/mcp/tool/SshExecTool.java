package com.cloudops.mcp.tool;

import com.cloudops.asset.dto.AssetResponse;
import com.cloudops.asset.domain.SshAuthType;
import com.cloudops.asset.domain.SshCredential;
import com.cloudops.asset.service.AssetService;
import com.cloudops.mcp.McpTool;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.stereotype.Component;

/**
 * Executes a single shell command on a managed asset over SSH and returns
 * stdout/stderr. Used by the AI agent when the model requests remote inspection
 * (e.g. {@code df -h}, {@code docker ps}). Risk classification and approval
 * gating happen in the approval module before this tool is invoked.
 */
@Component
public class SshExecTool implements McpTool {

    private final AssetService assetService;
    private final SshClient sshClient;

    public SshExecTool(AssetService assetService) {
        this.assetService = assetService;
        this.sshClient = SshClient.setUpDefaultClient();
        this.sshClient.start();
    }

    @Override
    public String name() {
        return "ssh_exec";
    }

    @Override
    public String description() {
        return "Execute a shell command on a managed Linux asset via SSH and return combined stdout/stderr. "
                + "Use for read-only diagnostics like df, free, docker ps, kubectl get. "
                + "Destructive commands require prior approval.";
    }

    @Override
    public String parametersJson() {
        return """
                {"type":"object","properties":{"assetId":{"type":"integer","description":"ID of the target asset"},"command":{"type":"string","description":"Shell command to execute"}},"required":["assetId","command"]}""";
    }

    @Override
    public String execute(Map<String, Object> arguments, ExecutionContext context) throws Exception {
        Long assetId = ((Number) arguments.get("assetId")).longValue();
        String command = String.valueOf(arguments.get("command"));

        AssetResponse asset = assetService.get(assetId);
        if (asset.host() == null) {
            return "Error: asset has no host configured";
        }

        SshCredential credential = assetService.getSshCredential(assetId);
        String secret = assetService.decryptSecret(credential);
        int port = asset.port() != null ? asset.port() : 22;

        try (ClientSession session = sshClient.connect(credential.getUsername(), asset.host(), port)
                .verify(15_000).getSession()) {
            if (credential.getAuthType() == SshAuthType.PASSWORD) {
                session.addPasswordIdentity(secret);
            }
            session.auth().verify(15_000);

            try (ClientChannel channel = session.createExecChannel(command);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                channel.setOut(out);
                channel.setErr(out);
                channel.open().verify(15_000);
                channel.waitFor(java.util.EnumSet.of(ClientChannelEvent.CLOSED), 30_000);
                return out.toString();
            }
        }
    }
}
