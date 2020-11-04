package com.sbt.kuberpunk.strategy;

public interface SubstitutionStrategy {
    void setNamespace();
    void setService();
    void scaleProxiedDeployment();
    void createServiceAccount();
    void createConfig();
    void createProxyDeployment();
}