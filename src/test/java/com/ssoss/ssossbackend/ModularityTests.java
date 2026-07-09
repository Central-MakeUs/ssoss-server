package com.ssoss.ssossbackend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

@DisplayName("ApplicationModules")
class ModularityTests {

    @Test
    @DisplayName("애플리케이션 모듈 구조가 Modulith 규칙을 위반하지 않는다")
    void verifiesModuleStructure() {
        ApplicationModules.of(SsossBackendApplication.class).verify();
    }
}
