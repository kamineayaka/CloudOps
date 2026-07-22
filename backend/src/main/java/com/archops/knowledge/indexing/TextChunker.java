package com.archops.knowledge.indexing;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Splits long documents into overlapping chunks suitable for embedding.
 * Uses character-based windows with paragraph-aware boundaries.
 */
@Component
public class TextChunker {

    public List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.strip();
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());
            if (end < normalized.length()) {
                int breakAt = findBreakPoint(normalized, start, end);
                if (breakAt > start) {
                    end = breakAt;
                }
            }
            String piece = normalized.substring(start, end).strip();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return chunks;
    }

    private int findBreakPoint(String text, int start, int end) {
        for (int i = end; i > start + end / 2; i--) {
            char c = text.charAt(i - 1);
            if (c == '\n' || c == '。' || c == '！' || c == '？' || c == '.' || c == '!' || c == '?') {
                return i;
            }
        }
        int space = text.lastIndexOf(' ', end);
        return space > start ? space : end;
    }
}
