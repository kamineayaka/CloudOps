package com.cloudops.knowledge.indexing;

/**
 * Converts float arrays to the textual format expected by pgvector casts.
 */
public final class VectorUtils {

    private VectorUtils() {}

    public static String toPgVector(float[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("embedding vector must not be empty");
        }
        StringBuilder sb = new StringBuilder(values.length * 8 + 2);
        sb.append('[');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
