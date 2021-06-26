package com.kuberpunk.redirection;

import com.kuberpunk.cloudcomponents.creaters.CloudComponentsCreator;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.fabric8.openshift.client.OpenShiftClient;

import java.util.HashMap;
import java.util.Map;

public class SshServiceCreator implements CloudComponentsCreator {

    private final OpenShiftClient openShiftClient;

    public SshServiceCreator(OpenShiftClient openShiftClient) {
        this.openShiftClient = openShiftClient;
    }

    private String HOST_PORT= "30022";

    @Override
    public void checkOrCreate(String namespace) {
        createService(namespace);
    }

    private void createService(String namespace) {
        try {
            openShiftClient.services().inNamespace(namespace).withName("python-sshd").require();
        } catch (ResourceNotFoundException e) {
            doCreateService(namespace);
        }
    }
    private void doCreateService(String namespace) {
        Map<String, String> selector = new HashMap<>();
        selector.put("run", "ssh");

        Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName("python-sshd")
                    .addToLabels("run", "ssh")
                .endMetadata()
                .withNewSpec().withExternalTrafficPolicy("Cluster")
                    .addNewPort()
                    .withProtocol("TCP")
                    .withPort(2022)
                        .withTargetPort(new IntOrString(22))
                    .withNodePort(Integer.valueOf(HOST_PORT))
                    .endPort()
                .withSelector(selector)
                .endSpec()
                .build();

        openShiftClient.services().inNamespace(namespace).createOrReplace(service);
    }

}

