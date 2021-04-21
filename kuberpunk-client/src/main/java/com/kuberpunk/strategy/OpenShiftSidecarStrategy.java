package com.kuberpunk.strategy;

import com.kuberpunk.input.InputClusterArgs;
import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpenShiftSidecarStrategy implements SubstitutionStrategy, ProxyLifeCycleStrategy {

    private final SubstitutionStrategy firstChain;

    @Override
    public void startProxying(InputClusterArgs inputClusterArgs) {
        ConfigMapKeySelector configMapKeySelector = new ConfigMapKeySelector();
        configMapKeySelector.setName(inputClusterArgs.getService() + "-kuberpunk-replacement");
        configMapKeySelector.setKey("localhost.route");

        apply(inputClusterArgs);
    }


    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        firstChain.apply(inputClusterArgs);
    }

    @Override
    public void close() {

    }
}
