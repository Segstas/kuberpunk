package com.sbt.kuberpunk.cloudcomponents;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ServiceAccountWorker implements IServiceAccountWorker {

    private String serviceName;
    private String namespace;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    KubernetesClient cloudClient;

    public ServiceAccountWorker(KubernetesClient client) {
        this.cloudClient = client;
    }

    @Override
    public void createServiceAccount() {
        ServiceAccount kuberpunkServiceAccount = new ServiceAccountBuilder().withNewMetadata().withName("kuberpunk-service-account").endMetadata().build();
        cloudClient.serviceAccounts().inNamespace(namespace).createOrReplace(kuberpunkServiceAccount);
    }
}
