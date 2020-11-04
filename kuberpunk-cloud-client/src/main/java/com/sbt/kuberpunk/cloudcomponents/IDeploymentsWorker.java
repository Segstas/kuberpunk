package com.sbt.kuberpunk.cloudcomponents;

public interface IDeploymentsWorker {
    void editProxiedDeployment();

    void createSubstituteDeployment();
    void deleteSubstituteDeployment();
}