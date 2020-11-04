package com.sbt.kuberpunk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

  @Autowired
  private ClientConfig config;


  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
            .route(p -> p.path("/**")
                    .filters(f -> f.addRequestHeader("Hello", "World")
                            .filter(routeToLocalServicesFilter))
                    .uri("lb://employee")) ///заменить хардкод
            .build();
  }


/*  @RequestMapping("/{serviceId}")
  public String redirectToDesired(@PathVariable("serviceId") String serviceId) {
    restTemplate.
    return repository.findByOrganization(organizationId);
  }*/

  @Autowired
  RestTemplate restTemplate;

  @Autowired
  private DiscoveryClient discoveryClient;

  RouteToLocalServicesFilter routeToLocalServicesFilter = new RouteToLocalServicesFilter(discoveryClient);


/*
  @GetMapping("/test")
  @ResponseBody
  public String invokeTestService() {
    List<ServiceInstance> testServiceInstances = this.discoveryClient.getInstances("test-service");
    return restTemplate.getForObject(testServiceInstances.get(0).getUri(), String.class);
  }

  @GetMapping("/services")
  public List<String> services() {
    return this.discoveryClient.getServices();
  }

  @GetMapping("/services/{serviceId}")
  public List<ServiceInstance> servicesById(@PathVariable("serviceId") String serviceId) {
    return this.discoveryClient.getInstances(serviceId);
  }
*/

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }
}
