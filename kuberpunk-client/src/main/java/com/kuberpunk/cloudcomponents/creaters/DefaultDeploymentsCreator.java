package com.kuberpunk.cloudcomponents.creaters;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

public class DefaultDeploymentsCreator implements CloudComponentsCreator {

    private final OpenShiftClient openShiftClient;

    @Value( "${cloud.controller.image.name}" )
    private String cloudControllerImageName;

    @Value( "${cloud.controller.image.tag}")
    private String cloudControllerImageTag;


    @Value( "${cloud.controller.service.name}")
    private String cloudControllerServiceName;

    public DefaultDeploymentsCreator(OpenShiftClient openShiftClient) {
        this.openShiftClient = openShiftClient;
    }

    @Override
    public void checkOrCreate(String namespace) {
        createDeployment(namespace);
    }

    private void createDeployment(String namespace) {
        try {
            openShiftClient.apps().deployments().inNamespace(namespace).withName(cloudControllerServiceName).require();
        } catch (ResourceNotFoundException e) {
            doCreateDeployment(namespace);
        }
    }

        private void doCreateDeployment(String namespace) {
            Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(cloudControllerServiceName)
                    .addToLabels("app", cloudControllerServiceName)
                .endMetadata()
                    .withNewSpec()
                        .withReplicas(1)
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", cloudControllerServiceName)
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                    .withName(cloudControllerServiceName)
                                    .withImage(cloudControllerImageName + ":" + cloudControllerImageTag)
                                    .addNewPort()
                                        .withContainerPort(80)
                                    .endPort()
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                        .withNewSelector()
                            .addToMatchLabels("app", cloudControllerServiceName)
                        .endSelector()
                    .endSpec()
                .build();

        openShiftClient.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
        try {
            openShiftClient.apps().deployments().inNamespace(namespace)
                    .withName(cloudControllerServiceName).waitUntilReady(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

