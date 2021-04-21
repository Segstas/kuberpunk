package com.kuberpunk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {

    @Bean
    ClientsHolder clientsHolder() {
        return new ClientsHolderImpl();
    }

    @Bean
    RouteToLocalServicesFilter routeToLocalServicesFilter(){
        return new RouteToLocalServicesFilter();
    }
}
