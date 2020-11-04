package com.sbt.kuberpunk.strategy;

import java.io.Closeable;

public interface ProxyLifeCycleStrategy extends Closeable {
    void startProxying();
}
