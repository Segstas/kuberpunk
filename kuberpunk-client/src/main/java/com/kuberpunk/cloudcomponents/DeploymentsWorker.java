package com.kuberpunk.cloudcomponents;

import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;

public interface DeploymentsWorker {

    void editProxiedDeployment(String serviceName, String namespace);


    void createSubstituteDeployment(String serviceName, String namespace,
                                    ConfigMapKeySelector configMapKeySelector);
}
