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
        String path = "/register-client/{serviceId}/{ip}";
        return redirectToService(serviceId, ip, "NaMeSpAcE", path);///todo
    }

    @PostMapping("/unregister-client/{serviceId}/{ip}")
    boolean unRegisterClient(@PathVariable("serviceId") String serviceId, @PathVariable("ip") String ip) {
        LOGGER.info("Catch \"Unregister Client\" command from IP: {} for service: {} ", ip, serviceId);
        String path = "/unregister-client/{serviceId}/{ip}";
        return redirectToService(serviceId, ip, "NaMeSpAcE", path);///todo

    }

    private boolean redirectToService(String serviceName, String ip, String namespace, String path) {
        ClientData clientData = new ClientData(serviceName, ip);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ClientData> request =
                new HttpEntity<>(clientData);
        List<ServiceInstance> cloudControllerInstances = this.discoveryClient.getInstances(serviceName);
        URI cloudControllerUri = cloudControllerInstances.get(0).getUri();
        LOGGER.info("Found service: {} instance with URI: {}. " +
                " Trying to redirect",serviceName, cloudControllerUri);
        ResponseEntity<String> responseEntityStr = restTemplate.
                postForEntity(cloudControllerUri + path, request, String.class, clientData.getService(), clientData.getClientAddress());
        LOGGER.info("Redirect finished with status: {}",responseEntityStr.getStatusCode());
        return !responseEntityStr.getStatusCode().isError();
    }
}
