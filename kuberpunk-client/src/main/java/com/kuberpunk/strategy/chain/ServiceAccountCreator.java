package com.kuberpunk.strategy.chain;

import com.kuberpunk.cloudcomponents.ServiceAccountWorker;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class ServiceAccountCreator implements SubstitutionStrategy {

    @Value("${service.account.name}")
    private String serviceAccountName= "kuberpunk-service-account"; ///TODO


    private final SubstitutionStrategy next;
    private final ServiceAccountWorker serviceAccountWorker;
    private final OpenShiftClient openShiftClient;

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        if (openShiftClient.serviceAccounts().
                inNamespace(inputClusterArgs.getNamespace()).withName(serviceAccountName) == null) {
            serviceAccountWorker.createServiceAccount(openShiftClient, inputClusterArgs.getNamespace());
        }
        next.apply(inputClusterArgs);
    }
}
