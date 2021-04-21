package com.kuberpunk.controller.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ClientData {
    private final String service;
    private final String clientAddress;
}
