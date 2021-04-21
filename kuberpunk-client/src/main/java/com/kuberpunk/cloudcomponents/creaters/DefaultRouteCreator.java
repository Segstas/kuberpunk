package com.kuberpunk.cloudcomponents.creaters;

import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Value;

public class DefaultRouteCreator implements CloudComponentsCreator {

    private final OpenShiftClient openShiftClient;

    @Value( "${cloud.controller.service.name}")
    private String cloudControllerServiceName = "kuberpunk-cloud-controller"; ///TODO

    @Value( "${cloud.controller.service.route.name}")
    private String cloudControllerServiceRoute= "kuberpunk-cloud-route"; ///TODO

    @Value( "${cloud.controller.service.port.name}")
    private String cloudControllerServicePortName;


    public DefaultRouteCreator(OpenShiftClient openShiftClient) {
        this.openShiftClient = openShiftClient;
    }

    @Override
    public void checkOrCreate(String namespace) {
        createRoute(namespace);
    }

    private void createRoute(String namespace) {
        try {
            openShiftClient.routes().inNamespace(namespace).withName(cloudControllerServiceRoute).require();
        } catch (ResourceNotFoundException e) {
            Route route = new RouteBuilder()
                    .editOrNewMetadata()
                        .withName(cloudControllerServiceRoute)
                        .withNamespace(namespace)
                        .addToLabels("app", cloudControllerServiceName)
                    .endMetadata()
                    .editOrNewSpec()
                        .editOrNewPort()
                            .withNewTargetPort(cloudControllerServicePortName)
                        .endPort()
                        .withNewTo()
                            .withName(cloudControllerServiceName)
                            .withKind("Service")
                        .endTo()
                    .endSpec()
                    .build();

            OpenShiftClient openShift = openShiftClient.adapt(OpenShiftClient.class);
            openShift.routes().inNamespace(namespace).createOrReplace(route);
        }
    }
}

