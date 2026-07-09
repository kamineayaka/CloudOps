package com.cloudops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudOpsApplication.class, args);
    }
}
