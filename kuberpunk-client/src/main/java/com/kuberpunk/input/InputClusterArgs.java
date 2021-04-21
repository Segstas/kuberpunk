package com.kuberpunk.input;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class InputClusterArgs {

    @Nullable
    private String service;

    @Nullable
    private String namespace;

}
