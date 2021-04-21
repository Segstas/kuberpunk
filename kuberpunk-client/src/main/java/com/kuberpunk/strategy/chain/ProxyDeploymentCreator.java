package com.kuberpunk.strategy.chain;

import com.kuberpunk.strategy.SubstitutionStrategy;
import com.kuberpunk.cloudcomponents.DeploymentsWorker;
import com.kuberpunk.input.InputClusterArgs;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProxyDeploymentCreator implements SubstitutionStrategy {

    private final DeploymentsWorker deploymentsWorker;

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        deploymentsWorker.createSubstituteDeployment(inputClusterArgs.getService(),
                inputClusterArgs.getNamespace(), inputClusterArgs.getConfigMapKeySelector());
    }
}
