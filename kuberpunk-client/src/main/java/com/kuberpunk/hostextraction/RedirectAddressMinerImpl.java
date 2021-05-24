package com.kuberpunk.hostextraction;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RedirectAddressMinerImpl implements RedirectAddressMiner {

    private static final Logger logger = LoggerFactory.getLogger(RedirectAddressMinerImpl.class);

    @Override
    @SneakyThrows
    public String getRedirectAddress() {
        Set<String> targetAddress = new HashSet<>();
        try {
            Enumeration<NetworkInterface> ne = NetworkInterface.getNetworkInterfaces();
            while (ne.hasMoreElements()) {
                NetworkInterface net = ne.nextElement();
                Set<String> interfaceAddresses =
                        net.getInterfaceAddresses().
                                stream()
                                .filter(interfaceAddress ->
                                        interfaceAddress.getAddress().isSiteLocalAddress())
                                .filter(interfaceAddress -> net.getDisplayName().equals("bridge100")) ///TODO how to find ip
                                .map(interfaceAddress -> interfaceAddress.getAddress().getHostAddress())
                                .collect(Collectors.toSet());
                if (!interfaceAddresses.isEmpty()) {
                    targetAddress.addAll(interfaceAddresses);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (targetAddress.stream().findFirst().isPresent()){
            logger.info("Local IP address {} was found", targetAddress.stream().findFirst().get());
            return targetAddress.stream().findFirst().get();
        }
        return "";
    }
}