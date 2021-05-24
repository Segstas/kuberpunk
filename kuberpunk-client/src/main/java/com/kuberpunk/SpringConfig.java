package com.kuberpunk;

import com.kuberpunk.cloudcomponents.creaters.CloudComponentsCreator;
import com.kuberpunk.cloudcomponents.creaters.DefaultDeploymentsCreator;
import com.kuberpunk.cloudcomponents.creaters.DefaultRouteCreator;
import com.kuberpunk.cloudcomponents.creaters.DefaultServiceCreator;
import com.kuberpunk.hostextraction.RestRedirectInformationPusherImpl;
import com.kuberpunk.input.ArgumentParser;
import com.kuberpunk.redirection.TunnelCreator;
import com.kuberpunk.strategy.SubstitutionStrategy;
import com.kuberpunk.strategy.chain.CloudControllerHandler;
import com.kuberpunk.cloudcomponents.ServiceAccountWorker;
import com.kuberpunk.cloudcomponents.ServiceAccountWorkerImpl;
import com.kuberpunk.hostextraction.RedirectAddressMiner;
import com.kuberpunk.hostextraction.RedirectAddressMinerImpl;
import com.kuberpunk.hostextraction.RedirectInformationPusher;
import com.kuberpunk.strategy.OpenShiftSidecarStrategy;
import com.kuberpunk.strategy.ProxyLifeCycleStrategy;
import com.kuberpunk.strategy.chain.ConfigPusher;
import com.kuberpunk.strategy.chain.ServiceAccountCreator;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Bean
    ArgumentParser argumentParser(ProxyLifeCycleStrategy proxyLifeCycleStrategy){
        return new ArgumentParser(proxyLifeCycleStrategy);
    }

    @Bean
    RedirectAddressMiner redirectAddressMiner() {
        return new RedirectAddressMinerImpl();
    }

    @Bean
    ServiceAccountWorker serviceAccountWorker() {
        return new ServiceAccountWorkerImpl();
    }

    @Bean
    OpenShiftClient openShiftClient() {
        return new DefaultOpenShiftClient();
    }

    @Bean
    RedirectInformationPusher redirectInformationPusher(OpenShiftClient openShiftClient,
                                                        RedirectAddressMiner redirectAddressMiner) {
        return new RestRedirectInformationPusherImpl(openShiftClient, redirectAddressMiner);
    }

    @Bean
    CloudComponentsCreator deploymentsCreator(OpenShiftClient openShiftClient) {
        return new DefaultDeploymentsCreator(openShiftClient);
    }

    @Bean
    CloudComponentsCreator serviceCreator(OpenShiftClient openShiftClient) {
        return new DefaultServiceCreator(openShiftClient);
    }

    @Bean
    CloudComponentsCreator routeCreator(OpenShiftClient openShiftClient) {
        return new DefaultRouteCreator(openShiftClient);
    }

    @Bean
    SubstitutionStrategy configPusher(RedirectInformationPusher redirectInformationPusher) {
        return new ConfigPusher(redirectInformationPusher);
    }

    @Bean
    SubstitutionStrategy cloudControllerHandler(SubstitutionStrategy configPusher,
                                                CloudComponentsCreator deploymentsCreator,
                                                CloudComponentsCreator serviceCreator,
                                                CloudComponentsCreator routeCreator) {
        return new CloudControllerHandler(configPusher, deploymentsCreator, serviceCreator, routeCreator);
    }

    @Bean
    SubstitutionStrategy serviceAccountCreator(SubstitutionStrategy cloudControllerHandler,
                                               ServiceAccountWorker serviceAccountWorker,
                                               OpenShiftClient openShiftClient) {
        return new ServiceAccountCreator(cloudControllerHandler,
                serviceAccountWorker, openShiftClient);
    }

    @Bean
    ProxyLifeCycleStrategy proxyLifeCycleStrategy(SubstitutionStrategy serviceAccountCreator) {
        return new OpenShiftSidecarStrategy(serviceAccountCreator);
    }

    @Bean
    TunnelCreator tunnelCreator(OpenShiftClient openShiftClient) {
        return new TunnelCreator(openShiftClient);
    }
}

