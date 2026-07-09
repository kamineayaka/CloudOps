package com.cloudops.knowledge.indexing;

public class EmbeddingException extends RuntimeException {

    public EmbeddingException(String message) {
        super(message);
    }

    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
