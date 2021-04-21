package com.kuberpunk.strategy.chain;

import com.kuberpunk.cloudcomponents.creaters.CloudComponentsCreator;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CloudControllerHandler implements SubstitutionStrategy {

    private final SubstitutionStrategy next;
    private final CloudComponentsCreator deploymentsCreator;
    private final CloudComponentsCreator serviceCreator;
    private final CloudComponentsCreator routeCreator;

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        String namespace = inputClusterArgs.getNamespace();
        serviceCreator.checkOrCreate(namespace);
        deploymentsCreator.checkOrCreate(namespace);
        routeCreator.checkOrCreate(namespace);

        next.apply(inputClusterArgs);
    }
}
