package com.kuberpunk;

import com.kuberpunk.input.ArgumentParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

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
        ArgumentParser argumentParser = context.getBean(ArgumentParser.class);
        argumentParser.parse(args);
    }


}
