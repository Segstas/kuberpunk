package com.kuberpunk;

import com.kuberpunk.connection.client.ClientHostData;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ClientsHolderImpl implements ClientsHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientsHolderImpl.class);

    private final Map<String, Integer> activeClients = new HashMap<>();

    @Override
    public boolean addClient(ClientHostData clientHostData) {
        activeClients.put(clientHostData.getIp(), clientHostData.getPort());
        LOGGER.info("Client {}:{} added to Clients Holder", clientHostData.getIp(), clientHostData.getPort());
        return true;
    }

    @Override
    public boolean deleteClient(ClientHostData clientHostData) {
        Integer deletedClientPort = activeClients.remove(clientHostData.getIp());
        LOGGER.info("Client {}:{} deleted from Clients Holder", clientHostData.getIp(), deletedClientPort);
        return true;
    }

    @Override
    public boolean contains(String clientIp) {
        return activeClients.containsKey(clientIp);
    }

    @Override
    public Integer getPort(String clientHostData) {
        return activeClients.get(clientHostData);
    }

    @Override
    public Collection<ClientHostData> getClients() {
        LOGGER.info("Getting clients list");
        Set<Map.Entry<String, Integer>> entrySet = activeClients.entrySet();
        return new ArrayList<>(entrySet)
                .stream()
                .map(entry -> new ClientHostData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
