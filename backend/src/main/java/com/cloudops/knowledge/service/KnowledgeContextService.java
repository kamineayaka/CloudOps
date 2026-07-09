package com.cloudops.knowledge.service;

import com.cloudops.common.config.RagProperties;
import com.cloudops.knowledge.domain.ArchitectureSnapshot;
import com.cloudops.knowledge.domain.KnowledgeSourceType;
import com.cloudops.knowledge.domain.WorkLog;
import com.cloudops.knowledge.repository.ArchitectureSnapshotRepository;
import com.cloudops.knowledge.repository.KbChunkRepository;
import com.cloudops.knowledge.repository.WorkLogRepository;
import com.cloudops.knowledge.retrieval.RagRetrievalService;
import com.cloudops.knowledge.retrieval.ScoredChunk;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeContextService {

    private final RagProperties ragProperties;
    private final ArchitectureSnapshotRepository snapshotRepository;
    private final WorkLogRepository workLogRepository;
    private final KbChunkRepository kbChunkRepository;
    private final RagRetrievalService ragRetrievalService;

    public KnowledgeContextService(
            RagProperties ragProperties,
            ArchitectureSnapshotRepository snapshotRepository,
            WorkLogRepository workLogRepository,
            KbChunkRepository kbChunkRepository,
            RagRetrievalService ragRetrievalService) {
        this.ragProperties = ragProperties;
        this.snapshotRepository = snapshotRepository;
        this.workLogRepository = workLogRepository;
        this.kbChunkRepository = kbChunkRepository;
        this.ragRetrievalService = ragRetrievalService;
    }

    @Transactional(readOnly = true)
    public String buildContextSnippet(String userQuery) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Current Architecture\n");
        snapshotRepository.findTopByOrderByVersionDesc()
                .ifPresentOrElse(
                        s -> sb.append(s.getSummary() != null ? s.getSummary() : s.getContent()).append('\n'),
                        () -> sb.append("No architecture snapshot recorded yet.\n"));

        if (ragProperties.enabled() && userQuery != null && !userQuery.isBlank()) {
            List<ScoredChunk> chunks = ragRetrievalService.retrieve(userQuery);
            if (!chunks.isEmpty()) {
                sb.append("\n## Relevant Knowledge (semantic retrieval)\n");
                for (ScoredChunk chunk : chunks) {
                    sb.append("- [")
                            .append(chunk.sourceType().name())
                            .append(" score=")
                            .append(String.format(Locale.US, "%.2f", chunk.similarity()))
                            .append("] ")
                            .append(chunk.content())
                            .append('\n');
                }
            }
        }

        sb.append("\n## Recent Work Logs\n");
        List<WorkLog> logs = workLogRepository.findTop20ByOrderByCreatedAtDesc();
        if (logs.isEmpty()) {
            sb.append("No work logs yet.\n");
        } else {
            for (WorkLog log : logs) {
                sb.append("- [").append(log.getCreatedAt()).append("] ")
                        .append(log.getSummary()).append('\n');
            }
        }

        sb.append("\n## Knowledge Index\n");
        sb.append("- RAG enabled: ").append(ragProperties.enabled()).append('\n');
        sb.append("- Indexed chunks: ").append(kbChunkRepository.count()).append('\n');
        sb.append("- By source: architecture=")
                .append(kbChunkRepository.countBySourceType(KnowledgeSourceType.ARCHITECTURE))
                .append(", work_log=")
                .append(kbChunkRepository.countBySourceType(KnowledgeSourceType.WORK_LOG))
                .append(", manual=")
                .append(kbChunkRepository.countBySourceType(KnowledgeSourceType.MANUAL))
                .append('\n');

        return sb.toString();
    }
}
