package com.cloudops.terminal;

import com.cloudops.asset.domain.SshAuthType;
import com.cloudops.asset.domain.SshCredential;
import com.cloudops.asset.dto.AssetResponse;
import com.cloudops.asset.service.AssetService;
import com.cloudops.common.config.WebSocketEndpoint;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Web SSH terminal. Bridges a browser xterm.js session to a remote SSH shell
 * via Apache MINA SSHD. Connection lifecycle:
 *   1. Client opens ws://host/ws/terminal?token=JWT&amp;assetId=N
 *   2. Server validates JWT, resolves asset SSH credentials
 *   3. Server opens SSH Shell channel and pipes I/O both directions
 *   4. On close, SSH session and channel are cleaned up
 */
@Component
@WebSocketEndpoint("/ws/terminal")
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TerminalWebSocketHandler.class);
    private final AssetService assetService;
    private final SshClient sshClient;
    private final ConcurrentMap<String, TerminalSession> activeSessions = new ConcurrentHashMap<>();

    public TerminalWebSocketHandler(AssetService assetService) {
        this.assetService = assetService;
        this.sshClient = SshClient.setUpDefaultClient();
        this.sshClient.start();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) throws Exception {
        Long assetId = parseAssetId(extractQuery(wsSession, "assetId"));
        Long userId = (Long) wsSession.getAttributes().get("userId");
        if (userId == null || assetId == null) {
            wsSession.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        try {
            AssetResponse asset = assetService.get(assetId);
            if (asset.host() == null) {
                wsSession.sendMessage(new TextMessage("\r\n[CloudOps] Asset has no host configured\r\n"));
                wsSession.close(CloseStatus.SERVER_ERROR);
                return;
            }

            SshCredential credential = assetService.getSshCredential(assetId);
            String secret = assetService.decryptSecret(credential);
            int port = asset.port() != null ? asset.port() : 22;

            ClientSession session = sshClient.connect(credential.getUsername(), asset.host(), port)
                    .verify(15_000)
                    .getSession();

            if (credential.getAuthType() == SshAuthType.PASSWORD) {
                session.addPasswordIdentity(secret);
            } else {
                java.security.KeyPair keyPair = loadKeyPair(secret);
                session.addPublicKeyIdentity(keyPair);
            }
            session.auth().verify(15_000);

            ChannelShell channel = session.createShellChannel();
            channel.setPtyType("xterm-256color");
            channel.open().verify(15_000);

            OutputStream inputToShell = channel.getInvertedIn();
            InputStream remoteOut = channel.getInvertedOut();

            Thread reader = new Thread(() -> {
                byte[] buf = new byte[4096];
                int n;
                try {
                    while ((n = remoteOut.read(buf)) > 0 && wsSession.isOpen()) {
                        wsSession.sendMessage(new TextMessage(new String(buf, 0, n)));
                    }
                } catch (Exception ignored) {
                }
            }, "ssh-reader-" + wsSession.getId());
            reader.setDaemon(true);
            reader.start();

            activeSessions.put(wsSession.getId(), new TerminalSession(session, channel, inputToShell, reader));
            log.info("Terminal session opened: {} -> {}@{}:{}", wsSession.getId(), credential.getUsername(), asset.host(), port);
        } catch (Exception ex) {
            log.error("Failed to open SSH terminal for asset {}", assetId, ex);
            wsSession.sendMessage(new TextMessage("\r\n[CloudOps] SSH connection failed: " + ex.getMessage() + "\r\n"));
            wsSession.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession wsSession, TextMessage message) throws Exception {
        TerminalSession ts = activeSessions.get(wsSession.getId());
        if (ts != null) {
            ts.inputToShell.write(message.asBytes());
            ts.inputToShell.flush();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) {
        TerminalSession ts = activeSessions.remove(wsSession.getId());
        if (ts != null) {
            closeQuietly(ts);
            log.info("Terminal session closed: {}", wsSession.getId());
        }
    }

    private java.security.KeyPair loadKeyPair(String pem) {
        // Private-key auth loads an OpenSSH/PEM key. Full PEM parsing is handled
        // by MINA's security providers in production; for the MVP the password
        // flow is the primary path. This is a hook for phase 2 polish.
        throw new UnsupportedOperationException(
                "Private key SSH auth requires a configured key loader; use password auth for now");
    }

    private String extractQuery(WebSocketSession session, String key) {
        String query = session.getUri().getQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    private Long parseAssetId(String raw) {
        try {
            return raw != null ? Long.valueOf(raw) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void closeQuietly(TerminalSession ts) {
        try { ts.channel.close(true); } catch (Exception ignored) {}
        try { ts.session.close(true); } catch (Exception ignored) {}
        ts.reader.interrupt();
    }

    private record TerminalSession(
            ClientSession session,
            ChannelShell channel,
            OutputStream inputToShell,
            Thread reader) {}
}
