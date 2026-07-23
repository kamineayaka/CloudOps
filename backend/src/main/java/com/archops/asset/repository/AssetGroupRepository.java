package com.archops.asset.repository;

import com.archops.asset.domain.AssetGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetGroupRepository extends JpaRepository<AssetGroup, Long> {

    Optional<AssetGroup> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
