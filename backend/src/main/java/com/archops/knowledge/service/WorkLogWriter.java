package com.archops.knowledge.service;

import com.archops.knowledge.domain.WorkLog;
import com.archops.knowledge.indexing.RagIndexTrigger;
import com.archops.knowledge.repository.WorkLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Central entry point for persisting work logs and triggering RAG indexing.
 */
@Service
public class WorkLogWriter {

    private final WorkLogRepository workLogRepository;
    private final RagIndexTrigger ragIndexTrigger;

    public WorkLogWriter(WorkLogRepository workLogRepository, RagIndexTrigger ragIndexTrigger) {
        this.workLogRepository = workLogRepository;
        this.ragIndexTrigger = ragIndexTrigger;
    }

    @Transactional
    public WorkLog save(WorkLog workLog) {
        WorkLog saved = workLogRepository.save(workLog);
        ragIndexTrigger.indexWorkLogAfterCommit(saved.getId());
        return saved;
    }
}
