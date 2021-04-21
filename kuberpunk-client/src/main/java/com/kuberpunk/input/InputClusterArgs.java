package com.kuberpunk.input;

import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;
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

    @Nullable
    private ConfigMapKeySelector configMapKeySelector;

}
