package com.cloudops.asset.repository;

import com.cloudops.asset.domain.SshCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SshCredentialRepository extends JpaRepository<SshCredential, Long> {
    Optional<SshCredential> findByAssetId(Long assetId);
}
