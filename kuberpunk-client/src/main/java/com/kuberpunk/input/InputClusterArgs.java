package com.kuberpunk.input;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class InputClusterArgs {

    private LifeCycleCommand cycleCommand;

    @Nullable
    private String service;

    @Nullable
    private String namespace;

    @Nullable
    private String port;

}
