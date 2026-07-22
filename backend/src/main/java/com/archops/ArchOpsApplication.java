package com.archops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArchOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchOpsApplication.class, args);
    }
}
