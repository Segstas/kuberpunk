package com.kuberpunk.strategy;

import com.kuberpunk.input.InputClusterArgs;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpenShiftSidecarStrategy implements SubstitutionStrategy, ProxyLifeCycleStrategy {

    private final SubstitutionStrategy firstChain;

    @Override
    public void startProxying(InputClusterArgs inputClusterArgs) {
        apply(inputClusterArgs);
    }

    @Override
    public void stopProxying(InputClusterArgs inputClusterArgs) {
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
