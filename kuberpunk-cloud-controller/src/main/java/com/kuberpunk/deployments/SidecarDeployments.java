package com.kuberpunk.deployments;

import com.kuberpunk.controller.RestCloudController;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = { CacheAutoConfiguration.class})///NEED?
@Import(RestCloudController.class)
public class SidecarDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(SidecarDeployments.class);

    public static void main(String... args) {
        SpringApplication.run(SidecarDeployments.class, args);
        LOGGER.info("*** Kuberpunk cloud controller v2.6 ***");
        startWatching();
    }

    private static void startWatching() { ///todo ADD watchig for different services in different threads of threadpool
        var client = new DefaultKubernetesClient();
        client.inNamespace(SidecarDeploymentsWatcher.NAMESPACE).apps()
                .deployments().watch(new SidecarDeploymentsWatcher(client));
    }

}