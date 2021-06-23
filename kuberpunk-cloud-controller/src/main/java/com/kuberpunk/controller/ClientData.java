package com.kuberpunk.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class ClientData {
    private final String service;
    private final String clientAddress;
    @Nullable
    private String port = null;

    public ClientData(String service, String clientAddress) {
        this.service = service;
        this.clientAddress = clientAddress;
    }

    public String generatePath() {
        StringBuilder pathBuilder = new StringBuilder("/register-client");
        if (!(service == null)) {
            pathBuilder.append("/{").append("service").append("}");
        }
        if (!(clientAddress == null)) {
            pathBuilder.append("/{").append("ip").append("}");
        }
        if (!(port == null)) {
            pathBuilder.append("/{").append("port").append("}");
        }
        return pathBuilder.toString();
    }
}
