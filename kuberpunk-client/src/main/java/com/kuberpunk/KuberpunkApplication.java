package com.kuberpunk;

import com.kuberpunk.input.ArgumentParser;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.ProxyLifeCycleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@Slf4j
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Import(SpringConfig.class)
public class KuberpunkApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;


    public static void main(String... args) {
        SpringApplication.run(KuberpunkApplication.class, args);
    }

    @Override
    public void run(String... args) {
        ArgumentParser argumentParser = new ArgumentParser();
        InputClusterArgs inputClusterArgs = argumentParser.parse(args);
        ProxyLifeCycleStrategy openShiftSidecarStrategy = context.getBean(ProxyLifeCycleStrategy.class);
        openShiftSidecarStrategy.startProxying(inputClusterArgs);
    }


}
