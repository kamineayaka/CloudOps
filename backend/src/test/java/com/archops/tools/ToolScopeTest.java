package com.archops.tools;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archops.common.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ToolScopeTest {

    @Test
    void allowsWhenNoTargetsConfigured() {
        assertThatCode(() -> ToolScope.assertInScope(List.of(), 99L)).doesNotThrowAnyException();
        assertThatCode(() -> ToolScope.assertInScope(null, 99L)).doesNotThrowAnyException();
    }

    @Test
    void allowsAssetInsideTargetUnion() {
        assertThatCode(() -> ToolScope.assertInScope(List.of(1L, 2L, 3L), 2L)).doesNotThrowAnyException();
    }

    @Test
    void rejectsAssetOutsideTargetUnion() {
        assertThatThrownBy(() -> ToolScope.assertInScope(List.of(1L, 2L), 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("TOOL_OUT_OF_SCOPE");
    }
}
