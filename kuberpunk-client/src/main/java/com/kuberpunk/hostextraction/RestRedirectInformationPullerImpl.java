package com.kuberpunk.hostextraction;

import com.kuberpunk.controller.api.ClientData;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.util.Locale;

@EnableDiscoveryClient
@RestController
public class RestRedirectInformationPullerImpl implements RedirectInformationPuller {

    private static final Logger logger = LoggerFactory.getLogger(RestRedirectInformationPullerImpl.class);

    private String serviceProtocol = "http";

    RedirectAddressMiner redirectAddressMiner;
    OpenShiftClient cloudClient;

    @Value("${cloud.controller.service.route.name}")
    private String cloudControllerRouteName;

    public RestRedirectInformationPullerImpl(OpenShiftClient client, RedirectAddressMiner redirectAddressMiner) {
        this.cloudClient = client;
        this.redirectAddressMiner = redirectAddressMiner;
    }

    @Override
    public void pullRedirectInformation(String serviceName, String namespace) {
        ClientData clientData = new ClientData(serviceName, redirectAddressMiner.getRedirectAddress());
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ClientData> request =
                new HttpEntity<>(clientData);
        String path = "/register-client/{serviceId}/{ip}";


        //todo add ping
        ResponseEntity<String> responseEntityStr =
                restTemplate.postForEntity(getRouteURI(serviceName, namespace) + path,
                        request, String.class, clientData.getService(), clientData.getClientAddress());
        logger.info("Request for adding service: {} in namespace:{} returned with status: {}",
                serviceName, namespace, responseEntityStr.getStatusCode());
    }

    private String getRouteURI(String serviceName, String namespace) {
        OpenShiftClient openShiftClient = cloudClient.adapt(OpenShiftClient.class);
        Route route = openShiftClient.routes().inNamespace(namespace).withName(cloudControllerRouteName).get();
        try {
            if (route != null) {
                return (serviceProtocol + "://" + route.getSpec().getHost()).toLowerCase(Locale.ROOT);
            }
        } catch (KubernetesClientException e) {
            if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                logger.warn("Could not lookup route:" + serviceName + " in namespace:" + namespace + ", due to: " + e.getMessage());
            }
        }
        return "";
    }
}
