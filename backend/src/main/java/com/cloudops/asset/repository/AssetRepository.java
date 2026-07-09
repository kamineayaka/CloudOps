package com.cloudops.asset.repository;

import com.cloudops.asset.domain.Asset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByParentId(Long parentId);
    List<Asset> findByKind(com.cloudops.asset.domain.AssetKind kind);
}
