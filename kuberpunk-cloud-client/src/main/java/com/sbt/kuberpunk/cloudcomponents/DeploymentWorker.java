package com.sbt.kuberpunk.cloudcomponents;

import com.sbt.kuberpunk.hostextraction.IRedirectInformationPuller;
import com.sbt.kuberpunk.hostextraction.RedirectInformationPuller;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;


public class DeploymentWorker implements IDeploymentsWorker {

    KubernetesClient cloudClient;
    IRedirectInformationPuller redirectInformationPuller;
    Deployment deployment;

    private String serviceName;
    private String namespace;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    public DeploymentWorker(KubernetesClient client, IRedirectInformationPuller redirectInformationPuller) {
        this.cloudClient = client;
        this.redirectInformationPuller = redirectInformationPuller;
    }

    @Override
    public void editProxiedDeployment() {
        String newSuffix = "-kuberpunk-old";
        cloudClient.apps().deployments().inNamespace(namespace).withName(serviceName).scale(0, true);

        String newName = serviceName + newSuffix;
        cloudClient.apps().deployments().inNamespace(namespace).withName(serviceName).edit()
                .editMetadata()
                .addToLabels("app", newName)
                .endMetadata()
                .editSpec()
                .endSpec()
                .done();
    }

    @Override
    public void createSubstituteDeployment() {


        deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(serviceName + "-kuberpunk-replacement")
                .addToLabels("app", serviceName)
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withNewTemplate()
                .withNewMetadata()
                .addToLabels("app", serviceName)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .addNewEnv()
                .withName("ROUTETOREDIRECT")
                .withNewValueFrom()
                .withConfigMapKeyRef(redirectInformationPuller.getConfigMapKeySelector())
                .endValueFrom()
                .endEnv()
                .withName(serviceName)
                .withImage("mabikon/kuberpunk-cloud-replacement-0.0.2")
                .addNewPort()
                .withContainerPort(80)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .withNewSelector()
                .addToMatchLabels("app", serviceName)
                .endSelector()
                .endSpec()
                .build();
////если под уже есть - создает второй
        deployment = cloudClient.apps().deployments().inNamespace(namespace).createOrReplace(deployment);


    }

    @Override
    public void deleteSubstituteDeployment() {
        cloudClient.resource(deployment).delete();
    }
}
