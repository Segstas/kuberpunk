package com.kuberpunk.strategy.chain;

import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import com.kuberpunk.cloudcomponents.DeploymentsWorker;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProxiedDeploymentScaler implements SubstitutionStrategy {

    private final SubstitutionStrategy next;
    private final DeploymentsWorker deploymentsWorker;

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        deploymentsWorker.editProxiedDeployment(inputClusterArgs.getService(), inputClusterArgs.getNamespace());
        next.apply(inputClusterArgs);
    }
}
