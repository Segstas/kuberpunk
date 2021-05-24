package com.kuberpunk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;


@RestController
public class RestCloudController {

    @Autowired
    private DiscoveryClient discoveryClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(RestCloudController.class);


    @PostMapping("/register-client/{serviceId}/{ip}")
    boolean registerClient(@PathVariable("serviceId") String serviceId, @PathVariable("ip") String ip) {
        LOGGER.info("Catch \"Register Client\" command from IP: {} for service: {} ", ip, serviceId);
        ClientData clientData = new ClientData(serviceId, ip);
        return redirectToService(clientData);///todo
    }

    @PostMapping("/unregister-client/{serviceId}/{ip}")
    boolean unRegisterClient(@PathVariable("serviceId") String serviceId, @PathVariable("ip") String ip) {
        LOGGER.info("Catch \"Unregister Client\" command from IP: {} for service: {} ", ip, serviceId);
        ClientData clientData = new ClientData(serviceId, ip);
        return redirectToService(clientData);///todo
    }

    @PostMapping("/register-client/{serviceId}/{ip}/{port}")
    boolean registerClientWithPort(@PathVariable("serviceId") String serviceId,
                                   @PathVariable("ip") String ip,
                                   @PathVariable("port") String port) {
        LOGGER.info("Catch \"Register Client\" command from IP: {} for service: {} with target port: {} ",
                ip, serviceId, port);
        ClientData clientData = new ClientData(serviceId, ip, port);
        return redirectToService(clientData);///todo
    }

    private boolean redirectToService(ClientData clientData) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ClientData> request =
                new HttpEntity<>(clientData);
        List<ServiceInstance> cloudControllerInstances = this.discoveryClient.getInstances(clientData.getService());
        URI cloudControllerUri = cloudControllerInstances.get(0).getUri();
        LOGGER.info("Found service: {} instance with URI: {}. " +
                " Trying to redirect", clientData.getService(), cloudControllerUri);
        ResponseEntity<String> responseEntityStr = restTemplate.
                postForEntity(cloudControllerUri + clientData.generatePath(), request, String.class,
                        clientData.getService(), clientData.getClientAddress(), clientData.getPort());
        LOGGER.info("Redirect finished with status: {}", responseEntityStr.getStatusCode());
        return !responseEntityStr.getStatusCode().isError();
    }
}
