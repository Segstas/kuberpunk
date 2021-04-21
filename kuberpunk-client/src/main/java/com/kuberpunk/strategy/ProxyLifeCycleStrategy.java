package com.kuberpunk.strategy;

import com.kuberpunk.input.InputClusterArgs;

import java.io.Closeable;

public interface ProxyLifeCycleStrategy extends Closeable {
    void startProxying(InputClusterArgs inputClusterArgs);
}
