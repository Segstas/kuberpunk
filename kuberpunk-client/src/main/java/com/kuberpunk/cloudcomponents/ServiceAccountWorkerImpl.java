package com.kuberpunk.cloudcomponents;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;

public class ServiceAccountWorkerImpl implements ServiceAccountWorker {

    @Value("${service.account.name:kuberpunk-service-account}")
    String serviceAccountName;

    @Override
    public void createServiceAccount(KubernetesClient cloudClient, String namespace) {
            ServiceAccount kuberpunkServiceAccount = new ServiceAccountBuilder().withNewMetadata().
                    withName(serviceAccountName).endMetadata().build();
            cloudClient.serviceAccounts().inNamespace(namespace).createOrReplace(kuberpunkServiceAccount);
    }
}
