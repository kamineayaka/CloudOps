package com.cloudops.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link org.springframework.web.socket.WebSocketHandler} for auto-registration
 * at the given path under {@code /ws}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketEndpoint {
    String value();
}
