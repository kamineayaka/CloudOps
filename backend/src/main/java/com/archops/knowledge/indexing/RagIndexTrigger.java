package com.archops.knowledge.indexing;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Schedules RAG indexing only after the surrounding transaction commits,
 * so async workers always see persisted source rows.
 */
@Component
public class RagIndexTrigger {

    private final KnowledgeIndexingService indexingService;

    public RagIndexTrigger(KnowledgeIndexingService indexingService) {
        this.indexingService = indexingService;
    }

    public void indexArchitectureAfterCommit(Long snapshotId) {
        runAfterCommit(() -> indexingService.scheduleIndexArchitecture(snapshotId));
    }

    public void indexWorkLogAfterCommit(Long workLogId) {
        runAfterCommit(() -> indexingService.scheduleIndexWorkLog(workLogId));
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
