package com.sbt.kuberpunk.hostextraction;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

public class RedirectInformationPuller implements IRedirectInformationPuller {

    IRedirectAddressMiner redirectAddressMiner;
    KubernetesClient cloudClient;

    private String serviceName;
    private String namespace;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public ConfigMapKeySelector getConfigMapKeySelector() {
        return configMapKeySelector;
    }

    private ConfigMapKeySelector configMapKeySelector;

    public RedirectInformationPuller(KubernetesClient client, IRedirectAddressMiner redirectAddressMiner) {
        this.cloudClient = client;
        this.redirectAddressMiner = redirectAddressMiner;
    }

    @Override
    public void pullRedirectInformation() {
        Resource<ConfigMap, DoneableConfigMap> configMapResource = cloudClient.configMaps().inNamespace(namespace).withName("localhost-to-route");
        configMapKeySelector = new ConfigMapKeySelector();
        configMapKeySelector.setName(serviceName + "-kuberpunk-replacement");
        configMapKeySelector.setKey("localhost.route");
        ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder()
                .withNewMetadata()
                .withName(serviceName + "-kuberpunk-replacement")
                .addToLabels("app", "serviceName")
                .endMetadata().
                        addToData("localhost.route", redirectAddressMiner.getRedirectAddress()). /////заменить хардкод
                build());
    }
}
