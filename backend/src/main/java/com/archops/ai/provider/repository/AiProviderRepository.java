package com.archops.ai.provider.repository;

import com.archops.ai.provider.domain.AiProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiProviderRepository extends JpaRepository<AiProvider, Long> {

    List<AiProvider> findByEnabledTrueOrderByNameAsc();

    long countByEnabledTrue();
}
