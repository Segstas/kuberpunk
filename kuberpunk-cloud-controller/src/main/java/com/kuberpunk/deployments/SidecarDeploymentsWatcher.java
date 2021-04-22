package com.kuberpunk.deployments;

import com.kuberpunk.controller.ServiceData;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@PropertySource("classpath:application.properties")
public class SidecarDeploymentsWatcher implements Watcher<Deployment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SidecarDeploymentsWatcher.class);
    private static final String SIDECAR_IMAGE_NAME = "mabikon/kuberpunk-cloud-replacement";
    private static final String SIDECAR_IMAGE_TAG = "2.9";

    private static final String SIDECAR_POD_NAME = "sidecar";
    private static final String SIDECAR_CONTAINER_NAME = "kuberpunk-cloud-proxy";
    public static final String NAMESPACE = "default";
    public static final Integer SIDECAR_TARGET_PORT = 1234;

    @Value("${cloud.controller.service.name:kuberpunk-cloud-controller}") ///TODO may be null because calling from static method
    private String cloudControllerServiceName = "kuberpunk-cloud-controller";

    private final KubernetesClient client;

    private final HashMap<ServiceData, List<ServicePort>> oldServicePortsMap = new HashMap<>();

    public SidecarDeploymentsWatcher(KubernetesClient client) {
        this.client = client;

    }

    public void eventReceived(Action action, Deployment deployment) {
        if (action == Action.ADDED) {
            var namespace = deployment.getMetadata().getNamespace();
            if (NAMESPACE.equals(namespace) && !isController(deployment)) {
                if (!alreadyHasSidecar(deployment)) {
                    createSidecar(deployment);
                } else {
                    LOGGER.info("Sidecar already existing for deployment {}", deployment.getMetadata().getName());
                }
            }
        }
        if (action == Action.DELETED) {
            var namespace = deployment.getMetadata().getNamespace();
            if (NAMESPACE.equals(namespace) && !isController(deployment)) {
                if (alreadyHasSidecar(deployment)) {
                    returnAsBeforeForService(deployment);
                } else {
                    LOGGER.info("Deployment's service {} was not proxied by Kuberpunk ", deployment.getMetadata().getName());
                }
            } else {
                if (NAMESPACE.equals(namespace) && isController(deployment)) {
                    onClose();
                }
            }
        }
    }

    private boolean isController(Deployment deployment) {
        var deploymentName = Optional.ofNullable(deployment.getMetadata().getName());
        var labels = Optional.ofNullable(deployment.getMetadata().getLabels());
        LOGGER.info("check if {} is a controller deployment: {}", deploymentName,
                cloudControllerServiceName.equals(deploymentName.get()) || cloudControllerServiceName.equals(
                        labels.orElse(new HashMap<>()).get("app")));
        return cloudControllerServiceName.equals(deploymentName.get()) ||
                cloudControllerServiceName.equals(
                        labels.orElse(new HashMap<>()).get("app")
                );
    }

    private void createSidecar(Deployment deployment) {
        String serviceName = deployment.getMetadata().getName();
        String namespace = deployment.getMetadata().getNamespace();

        Service targetService = client.services().inNamespace(namespace).withName(serviceName).get();
        Integer oldTargetPort = targetService.getSpec().getPorts().get(0).getTargetPort().getIntVal();

        client.apps().deployments().inNamespace(namespace)
                .withName(deployment.getMetadata().getName()).edit(
                        d -> new DeploymentBuilder(d)
                                .editSpec()
                                    .editTemplate()
                                        .editSpec()
                                            .addNewContainer()
                                            .withName(SIDECAR_CONTAINER_NAME)
                                                .addNewEnv()
                                                    .withName("PORT_TO_REDIRECT")
                                                    .withNewValue(String.valueOf(oldTargetPort))
                                                .endEnv()
                                            .withName(SIDECAR_POD_NAME)
                                            .withImage(SIDECAR_IMAGE_NAME + ":" + SIDECAR_IMAGE_TAG)
                                                .addNewPort()
                                                    .withContainerPort(SIDECAR_TARGET_PORT)
                                                .endPort()
                                            .endContainer()
                                        .endSpec()
                                    .endTemplate()
                                .endSpec()
                                .build()
                );
        List<ServicePort> oldServicePorts = client.services().inNamespace(namespace).withName(serviceName).get().getSpec().getPorts();
        oldServicePortsMap.put(new ServiceData(serviceName,namespace), oldServicePorts);
        List<ServicePort> newServicePorts = new ArrayList<>();
        oldServicePorts.forEach(servicePort -> newServicePorts.add(replaceTargetPorts(servicePort, SIDECAR_TARGET_PORT)));
        client.services().inNamespace(namespace).withName(serviceName).edit(
                s -> new ServiceBuilder(s)
                        .editSpec()
                        .removeAllFromPorts(oldServicePorts)
                        .addAllToPorts(newServicePorts)
                        .endSpec().build()
        );
    }

    private boolean alreadyHasSidecar(Deployment deployment) {
        List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
        LOGGER.info("check if deployment: {} already has sidecar: {},",deployment.getMetadata().getName(), containers
                .stream()
                .anyMatch(it -> it.getName().equals(SIDECAR_POD_NAME)));

        return containers
                .stream()
                .anyMatch(it -> it.getName().equals(SIDECAR_POD_NAME));
    }

    private ServicePort replaceTargetPorts(ServicePort port, Integer targetPort) {
            return new ServicePortBuilder(port).withTargetPort(new IntOrString(targetPort)).build();
        }


    @Override
    public void onClose() {
        oldServicePortsMap.keySet().forEach(this::returnAsBeforeForDeploymentAndService);
    }

    @Override
    public void onClose(WatcherException cause) {
        onClose();
    }

    private void returnAsBeforeForService(Deployment deployment) {
        String serviceName = deployment.getMetadata().getName();
        String namespace = deployment.getMetadata().getNamespace();
        ServiceData serviceToDelete = new ServiceData(serviceName, namespace);
        doReturnAsBeforeForService(serviceToDelete);
    }

    private void returnAsBeforeForDeploymentAndService(ServiceData serviceToDelete) {
        doReturnAsBeforeForService(serviceToDelete);
        doReturnAsBeforeForDeployment(serviceToDelete);
    }

    private void doReturnAsBeforeForService(ServiceData serviceToDelete) {
        String serviceName = serviceToDelete.getService();
        String namespace = serviceToDelete.getNamespace();
        List<ServicePort> replacedServicePorts = client.services().inNamespace(namespace).withName(serviceName).get().getSpec().getPorts();
        client.services().inNamespace(namespace).withName(serviceName).edit(
                s -> new ServiceBuilder(s)
                        .editSpec()
                        .removeAllFromPorts(replacedServicePorts)
                        .addAllToPorts(oldServicePortsMap.get(serviceToDelete))
                        .endSpec().build());
    }

    private void doReturnAsBeforeForDeployment(ServiceData serviceToDelete) {
        String serviceName = serviceToDelete.getService();
        String namespace = serviceToDelete.getNamespace();

        Container[] containersWithKuberpunkProxy = client.apps().deployments().inNamespace(namespace)
                .withName(serviceName).get().getSpec().getTemplate().getSpec().getContainers().stream()
                .filter(container -> container.getName().equals(SIDECAR_CONTAINER_NAME)).toArray(Container[]::new);

        client.apps().deployments().inNamespace(namespace)
                .withName(serviceName).edit(
                d -> new DeploymentBuilder(d)
                        .editSpec()
                            .editTemplate()
                                .editSpec()
                                    .removeFromContainers(containersWithKuberpunkProxy)
                                .endSpec()
                            .endTemplate()
                        .endSpec()
                        .build()
        );
    }
}