package com.kuberpunk.redirection;

import com.kuberpunk.cloudcomponents.creaters.CloudComponentsCreator;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.fabric8.openshift.client.OpenShiftClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SshPodCreator implements CloudComponentsCreator {

    private final OpenShiftClient openShiftClient;

    public SshPodCreator(OpenShiftClient openShiftClient) {
        this.openShiftClient = openShiftClient;
    }

    @Override
    public void checkOrCreate(String namespace) {
        createPod(namespace);
    }

    private void createPod(String namespace) {
        try {
            openShiftClient.pods().inNamespace(namespace)
                    .withName("python-sshd").require();
        } catch (ResourceNotFoundException e) {
            doCreatePod(namespace);
        }
    }
        private void doCreatePod(String namespace) {
            Map<String, String> label = new HashMap<>();
            label.put("run", "ssh");
            ContainerPort containerPort = new ContainerPort();
            containerPort.setContainerPort(22);
           Pod pod = new PodBuilder()
                   .withNewMetadata()
                    .withName("python-sshd")
                    .withLabels(label)
                   .endMetadata()
                   .withNewSpec()
                    .addNewContainer()
                    .withName("python-sshd")
                   .withImage("eugenes1/python-sshd:latest")
                   .withPorts()
                   .addNewPort()
                    .withContainerPort(22)
                    .endPort()
                   .endContainer()
                   .withRestartPolicy("Never")
                   .endSpec()
                   .build();

        openShiftClient.pods().inNamespace(namespace).createOrReplace(pod);
        try {
            openShiftClient.pods().inNamespace(namespace)
                    .withName("python-sshd").waitUntilReady(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

