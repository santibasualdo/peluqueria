package com.santi.turnero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TurneroApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurneroApplication.class, args);
    }
}
