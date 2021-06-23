package com.kuberpunk.cloudcomponents.creaters;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

public class DefaultServiceCreator implements CloudComponentsCreator {

    private final OpenShiftClient openShiftClient;

    @Value( "${cloud.controller.service.port}" )
    private String cloudControllerPort  = "1234";

    @Value( "${cloud.controller.service.port.name}" )
    private String cloudControllerPortName  = "kuberpunk-controller-main-port";

    @Value( "${cloud.controller.service.name}")
    private String cloudControllerServiceName;

    private String HOST_PORT= "30022";

    public DefaultServiceCreator(OpenShiftClient openShiftClient) {
        this.openShiftClient = openShiftClient;
    }

    @Override
    public void checkOrCreate(String namespace) {
        createService(namespace);
    }

    private void createService(String namespace) {
        try {
            openShiftClient.services().inNamespace(namespace).withName(cloudControllerServiceName).require();
        } catch (ResourceNotFoundException e) {
            doCreateService(namespace);
        }
    }

    private void doCreateService(String namespace) {
        Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName(cloudControllerServiceName)
                    .addToLabels("app", cloudControllerServiceName)
                .endMetadata()
                .withNewSpec()
                    .withSelector(Collections.singletonMap("app", cloudControllerServiceName))
                    .addNewPort()
                        .withName(cloudControllerPortName)
                    .withProtocol("TCP")
                    .withPort(80)
                        .withTargetPort(new IntOrString(Integer.valueOf(cloudControllerPort)))
                    .endPort()
                .endSpec()
                .build();

        openShiftClient.services().inNamespace(namespace).createOrReplace(service);
    }

}

