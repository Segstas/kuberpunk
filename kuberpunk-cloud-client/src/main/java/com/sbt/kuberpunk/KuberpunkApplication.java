package com.sbt.kuberpunk;

import com.sbt.kuberpunk.cloudcomponents.DeploymentWorker;
import com.sbt.kuberpunk.cloudcomponents.IDeploymentsWorker;
import com.sbt.kuberpunk.cloudcomponents.IServiceAccountWorker;
import com.sbt.kuberpunk.cloudcomponents.ServiceAccountWorker;
import com.sbt.kuberpunk.hostextraction.IRedirectAddressMiner;
import com.sbt.kuberpunk.hostextraction.IRedirectInformationPuller;
import com.sbt.kuberpunk.hostextraction.RedirectAddressMiner;
import com.sbt.kuberpunk.hostextraction.RedirectInformationPuller;
import com.sbt.kuberpunk.strategy.OpenShiftStrategy;
import com.sbt.kuberpunk.strategy.ProxyLifeCycleStrategy;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KuberpunkApplication {
    private static final Logger logger = LoggerFactory.getLogger(KuberpunkApplication.class);

    private static void log(String action, Object obj) {
        logger.info("{}: {}", action, obj);
    }


    public static void main(String[] args) {
        SpringApplication.run(KuberpunkApplication.class, args);
        OpenShiftClient openShiftClient = new DefaultOpenShiftClient();
        IRedirectAddressMiner redirectAddressMiner = new RedirectAddressMiner();
        IRedirectInformationPuller redirectInformationPuller = new RedirectInformationPuller(openShiftClient, redirectAddressMiner);
        IDeploymentsWorker deploymentsWorker = new DeploymentWorker(openShiftClient, redirectInformationPuller);
        IServiceAccountWorker serviceAccountWorker = new ServiceAccountWorker(openShiftClient);
        ProxyLifeCycleStrategy proxyLifeCycleStrategy = new OpenShiftStrategy(deploymentsWorker,serviceAccountWorker,redirectInformationPuller, redirectAddressMiner, openShiftClient);
        proxyLifeCycleStrategy.startProxying();
    }
}
