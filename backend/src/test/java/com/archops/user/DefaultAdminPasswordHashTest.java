package com.archops.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class DefaultAdminPasswordHashTest {

    private static final String ADMIN123_HASH =
            "$2a$10$ZKsyX8rPAqNxG.PIldklOec0L/pWQhwUIl2XNsSztB0xT10BudseG";

    @Test
    void seedHashMatchesAdmin123() {
        assertTrue(new BCryptPasswordEncoder().matches("admin123", ADMIN123_HASH));
    }
}
