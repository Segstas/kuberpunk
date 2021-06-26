package com.kuberpunk.redirection;

import com.kuberpunk.cloudcomponents.creaters.CloudComponentsCreator;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class SshutleTunnelCreator implements SubstitutionStrategy, TunnelCreator {

    private final OpenShiftClient cloudClient;
    private final CloudComponentsCreator sshServiceCreator;
    private final CloudComponentsCreator sshPodCreator;

    public SshutleTunnelCreator(OpenShiftClient cloudClient,
                                CloudComponentsCreator sshPodCreator,
                                CloudComponentsCreator sshServiceCreator
    ) {
        this.cloudClient = cloudClient;
        this.sshServiceCreator = sshServiceCreator;
        this.sshPodCreator = sshPodCreator;
    }

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        createTunnel(inputClusterArgs);
    }

    @Override
    @SneakyThrows
    public void createTunnel(InputClusterArgs inputClusterArgs) {

        OpenShiftClient openShiftClient = cloudClient.adapt(OpenShiftClient.class);

        PodList allPods = openShiftClient
                .pods()
                .inNamespace(inputClusterArgs.getNamespace())
                .list();

        StringBuilder servicePodsIps = new StringBuilder();
        if (inputClusterArgs.getNextServices() != null) {
            for (String nextService : inputClusterArgs.getNextServices()) {
                servicePodsIps.append(
                        allPods.getItems()
                                .stream()
                                .filter(pod -> pod.getMetadata().getName().contains(nextService))
                                .map(pod -> pod.getStatus().getPodIP() + ":" + 1234)
                                .collect(Collectors.joining(" "))
                );
            }
        }

        //TODO move functionality to controller sidecar
        sshPodCreator.checkOrCreate(inputClusterArgs.getNamespace());
        sshServiceCreator.checkOrCreate(inputClusterArgs.getNamespace());
        createSshuttleProcess(servicePodsIps);
    }

    private void createSshuttleProcess(StringBuilder servicePodsIps) throws IOException, InterruptedException {
        Path kuttlePath = Paths.get("kuttle");
        String[] args = new String[]{"sudo", "-S", "sshuttle", "-r", "python-sshd", "-e",
                kuttlePath.toAbsolutePath().toString(), servicePodsIps.toString()};
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(args);
        builder.inheritIO();
        Process sshuttleProcess = builder.start();
        sshuttleProcess.waitFor();
    }
}



