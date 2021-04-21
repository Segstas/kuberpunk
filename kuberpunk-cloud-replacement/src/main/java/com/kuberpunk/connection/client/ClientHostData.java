package com.kuberpunk.connection.client;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class ClientHostData {

    private final String ip;

    @Nullable
    private final Integer port;
}
