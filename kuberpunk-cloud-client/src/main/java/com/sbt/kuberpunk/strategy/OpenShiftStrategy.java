package com.sbt.kuberpunk.strategy;

import com.sbt.kuberpunk.cloudcomponents.IDeploymentsWorker;
import com.sbt.kuberpunk.cloudcomponents.IServiceAccountWorker;
import com.sbt.kuberpunk.hostextraction.IRedirectAddressMiner;
import com.sbt.kuberpunk.hostextraction.IRedirectInformationPuller;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

import java.io.IOException;

public class OpenShiftStrategy implements SubstitutionStrategy, ProxyLifeCycleStrategy {

    String namespace;
    String service;

    OpenShiftClient osClient;


    IDeploymentsWorker deploymentsWorker;
    IServiceAccountWorker serviceAccountWorker;
    IRedirectInformationPuller redirectInformationPuller;
    IRedirectAddressMiner redirectAddressMiner;

    public OpenShiftStrategy(IDeploymentsWorker deploymentsWorker,
                             IServiceAccountWorker serviceAccountWorker,
                             IRedirectInformationPuller redirectInformationPuller,
                             IRedirectAddressMiner redirectAddressMiner,
                             OpenShiftClient openShiftClient) {
        this.deploymentsWorker = deploymentsWorker;
        this.serviceAccountWorker = serviceAccountWorker;
        this.redirectInformationPuller = redirectInformationPuller;
        this.redirectAddressMiner = redirectAddressMiner;
        this.osClient = openShiftClient;
    }


    @Override
    public void setNamespace() {

    }

    @Override
    public void setService() {

    }

    @Override
    public void scaleProxiedDeployment() {
        deploymentsWorker.editProxiedDeployment();
    }

    @Override
    public void createServiceAccount() {
        serviceAccountWorker.createServiceAccount();
    }

    @Override
    public void createConfig() {
      redirectInformationPuller.pullRedirectInformation();
    }

    @Override
    public void createProxyDeployment() {
        deploymentsWorker.createSubstituteDeployment();
    }

    @Override
    public void startProxying() {
        setNamespace();
        setService();
        scaleProxiedDeployment();
        createServiceAccount();
        createConfig();
        createProxyDeployment();
    }

    @Override
    public void close() throws IOException {

    }
}
