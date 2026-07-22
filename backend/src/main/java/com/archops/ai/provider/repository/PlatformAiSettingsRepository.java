package com.archops.ai.provider.repository;

import com.archops.ai.provider.domain.PlatformAiSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformAiSettingsRepository extends JpaRepository<PlatformAiSettings, Short> {}
