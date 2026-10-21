package com.example.creditworthinesschecker;

import com.example.creditworthinesschecker.dto.PcrRequesterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PcrRequesterProperties.class)
public class CreditWorthinessCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditWorthinessCheckerApplication.class, args);
    }

}
