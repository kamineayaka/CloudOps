package com.cloudops.asset.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ssh_credentials")
public class SshCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(nullable = false, length = 64)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 16)
    private SshAuthType authType;

    @Column(name = "secret_cipher", nullable = false)
    private byte[] secretCipher;

    @Column(name = "secret_iv", nullable = false)
    private byte[] secretIv;

    @Column(name = "passphrase_hash", length = 255)
    private String passphraseHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public SshAuthType getAuthType() { return authType; }
    public void setAuthType(SshAuthType authType) { this.authType = authType; }
    public byte[] getSecretCipher() { return secretCipher; }
    public void setSecretCipher(byte[] secretCipher) { this.secretCipher = secretCipher; }
    public byte[] getSecretIv() { return secretIv; }
    public void setSecretIv(byte[] secretIv) { this.secretIv = secretIv; }
    public String getPassphraseHash() { return passphraseHash; }
    public void setPassphraseHash(String passphraseHash) { this.passphraseHash = passphraseHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
