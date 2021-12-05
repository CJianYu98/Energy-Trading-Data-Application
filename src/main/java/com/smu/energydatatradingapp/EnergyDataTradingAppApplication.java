package com.smu.energydatatradingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EnergyDataTradingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnergyDataTradingAppApplication.class, args);
    }

}