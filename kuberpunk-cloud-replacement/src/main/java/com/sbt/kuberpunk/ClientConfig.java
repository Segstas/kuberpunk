package com.sbt.kuberpunk;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "localhost-to-route")

public class ClientConfig {

    @Getter
    @Setter
    private String localRoute = "";
}
