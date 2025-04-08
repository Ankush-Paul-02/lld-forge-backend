package com.devmare.lldforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class LldforgeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LldforgeBackendApplication.class, args);
    }
}
