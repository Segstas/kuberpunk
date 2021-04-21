package com.kuberpunk.cloudcomponents;

import io.fabric8.kubernetes.client.KubernetesClient;

public interface ServiceAccountWorker {
    void createServiceAccount(KubernetesClient cloudClient, String namespace);
}
