package com.kuberpunk;

import com.kuberpunk.connection.client.ClientHostData;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@Slf4j
@Import(SpringConfiguration.class)
public class GatewayApplication {

  @Value("${cloud.controller.service.name:kuberpunk-cloud-controller}") ///TODO may be null because calling from static method
  private String cloudControllerServiceName = "kuberpunk-cloud-controller";

  @Autowired
  ClientsHolder clientsHolder;

  @Autowired
  RouteToLocalServicesFilter routeToLocalServicesFilter;

  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayApplication.class);

  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
            .route(p -> p.path("/**")
                    .filters(f -> f.filter(routeToLocalServicesFilter))
                    .uri("lb://" + cloudControllerServiceName)) ///TODO заменить хардкод lb для облака, http вне облака
            .build();
  }

  @PostMapping("/register-client/{serviceId}/{ip}")
  public boolean registerClient(@PathVariable("serviceId") String serviceId, @PathVariable("ip") String ip){
    String portForRedirect = System.getenv("PORT_TO_REDIRECT");
    if (portForRedirect == null) {
      LOGGER.info("Port is null in cloud config, using default {}", 8080);
      portForRedirect = String.valueOf(8080);
    }
    return clientsHolder.addClient(new ClientHostData(ip, Integer.valueOf(portForRedirect)));
  }

  @PostMapping("/unregister-client/{serviceId}/{ip}")
  public boolean unregisterClient(@PathVariable("serviceId") String serviceId, @PathVariable("ip") String ip){
    return clientsHolder.deleteClient(new ClientHostData(ip, null));
  }

  @GetMapping("/get-clients/")
  public String getClients(){
    return clientsHolder.getClients().toString();
  }


  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
    LOGGER.info("*** Kuberpunk cloud sidecar v1.0 ***");
  }
}






/*
  @RequestMapping("/{serviceId}")
  public String redirectToDesired(@PathVariable("serviceId") String serviceId) {
    restTemplate.
    return repository.findByOrganization(organizationId);
  }
*/


/*  @GetMapping("/test")
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
  }*/
