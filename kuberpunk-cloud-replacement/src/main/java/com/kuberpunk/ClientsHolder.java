package com.kuberpunk;

import com.kuberpunk.connection.client.ClientHostData;

import java.util.Collection;

public interface ClientsHolder {

    boolean addClient(ClientHostData clientHostData);

    boolean deleteClient(ClientHostData clientHostData);

    boolean contains(String clientHostData);

    Integer getPort(String clientHostData);

    Collection<ClientHostData> getClients();

}
