package com.chan.medmes;

import com.chan.medmes.global.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MedmesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedmesApplication.class, args);
    }

}
