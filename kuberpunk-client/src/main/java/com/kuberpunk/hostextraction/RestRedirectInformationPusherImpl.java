package com.kuberpunk.hostextraction;

import com.kuberpunk.controller.api.ClientData;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.redirection.TunnelCreator;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.util.Locale;

@RestController
public class RestRedirectInformationPusherImpl implements RedirectInformationPusher {

    private static final Logger logger = LoggerFactory.getLogger(RestRedirectInformationPusherImpl.class);

    private String serviceProtocol = "http";

    private TunnelCreator tunnelCreator;

    RedirectAddressMiner redirectAddressMiner;
    OpenShiftClient cloudClient;

    @Value("${cloud.controller.service.route.name}")
    private String cloudControllerRouteName;

    public RestRedirectInformationPusherImpl(OpenShiftClient client, RedirectAddressMiner redirectAddressMiner) {
        this.cloudClient = client;
        this.redirectAddressMiner = redirectAddressMiner;


        this.tunnelCreator = new TunnelCreator(client);

    }

    public void pushRedirectInformation(InputClusterArgs inputClusterArgs) {
        ClientData clientData = new ClientData(inputClusterArgs.getService(),
                redirectAddressMiner.getRedirectAddress(), inputClusterArgs.getPort());
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ClientData> request =
                new HttpEntity<>(clientData);
        //todo add ping
        ResponseEntity<String> responseEntityStr =
                restTemplate.postForEntity(getRouteURI(inputClusterArgs) +
                                inputClusterArgs.getCycleCommand().subPath + clientData.generatePath(),
                        request, String.class, clientData.getService(), clientData.getClientAddress(), clientData.getPort());
        logger.info("Request for adding service: {} in namespace:{} returned with status: {}",
                inputClusterArgs.getService(), inputClusterArgs.getNamespace(), responseEntityStr.getStatusCode());

        tunnelCreator.createTunnel(inputClusterArgs);
    }

    private String getRouteURI(InputClusterArgs inputClusterArgs) {
        OpenShiftClient openShiftClient = cloudClient.adapt(OpenShiftClient.class);
        Route route = openShiftClient.routes().inNamespace(inputClusterArgs.getNamespace()).withName(cloudControllerRouteName).get();
        try {
            if (route != null) {
                return (serviceProtocol + "://" + route.getSpec().getHost()).toLowerCase(Locale.ROOT);
            }
        } catch (KubernetesClientException e) {
            if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                logger.warn("Could not lookup route:" + inputClusterArgs.getService() + " in namespace:" + inputClusterArgs.getNamespace() + ", due to: " + e.getMessage());
            }
        }
        return "";
    }
}
