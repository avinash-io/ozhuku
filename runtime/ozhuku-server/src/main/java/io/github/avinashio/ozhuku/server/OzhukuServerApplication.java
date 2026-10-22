package io.github.avinashio.ozhuku.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class OzhukuServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(OzhukuServerApplication.class, args);
    }
}