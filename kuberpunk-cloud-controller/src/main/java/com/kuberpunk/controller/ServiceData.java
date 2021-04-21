package com.kuberpunk.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ServiceData {
    private final String service;
    private final String namespace;
}
