package com.ssoss.ssossbackend;

import com.ssoss.ssossbackend.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestSsossBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(SsossBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
