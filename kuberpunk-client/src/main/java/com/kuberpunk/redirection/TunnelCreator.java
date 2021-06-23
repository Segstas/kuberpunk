package com.kuberpunk.redirection;

import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TunnelCreator implements SubstitutionStrategy {

    OpenShiftClient cloudClient;

    @Value("${cloud.controller.service.name}")
    private final String cloudControllerServiceName = "kuberpunk-cloud-controller";

    public TunnelCreator(OpenShiftClient cloudClient) {
        this.cloudClient = cloudClient;
    }

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        createTunnel(inputClusterArgs);
    }

    @SneakyThrows
    private void createTunnel(InputClusterArgs inputClusterArgs) {
        OpenShiftClient openShiftClient = cloudClient.adapt(OpenShiftClient.class);
        PodList podList = openShiftClient.pods().inNamespace(inputClusterArgs.getNamespace()).withLabel("app", cloudControllerServiceName).list();
        Pod targetSshPod = podList.getItems().stream().filter(pod -> pod.getMetadata().getName().contains(cloudControllerServiceName))
                .collect(Collectors.toList()).get(0);
        String podName = "";
        if (targetSshPod != null) {
            podName = targetSshPod.getMetadata().getName();
        }
        PodList allPods = openShiftClient.pods().inNamespace(inputClusterArgs.getNamespace()).list();
        List<String> podIPs = new ArrayList<>();
        StringBuilder nextChainServiceIPs = new StringBuilder();
        if (inputClusterArgs.getNextServices() != null) {
            for (String nextService : inputClusterArgs.getNextServices()) {
                podIPs = allPods.getItems().stream().filter(pod -> pod.getMetadata().getName().contains(nextService))
                        .peek(addChainPodIP(nextChainServiceIPs))
                        .map(pod -> pod.getStatus().getPodIP())
                        .collect(Collectors.toList());
            }
        }
        if (nextChainServiceIPs.length() == 0) {
            for (String ip : podIPs) {
                nextChainServiceIPs.append(ip).append(' ');
            }
        }

        //TODO make commands in normal format

        ProcessBuilder pb = new ProcessBuilder("./sshuttle-over-k8s.sh");
        Process p = pb.start();
        /// p.waitFor();
        String[] args = new String[]{"sudo","-S","sshuttle", "-r", "python-sshd", "-e",
                "./kuttle",
                nextChainServiceIPs.toString()};

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(args);
        builder.inheritIO();
        Process process = builder.start();
        process.waitFor();
    }

    private Consumer<Pod> addChainPodIP(StringBuilder nextChainServiceIPs) {
        return pod -> nextChainServiceIPs.append(pod.getStatus().getPodIP()).append(':').append(1234);
    }
}



