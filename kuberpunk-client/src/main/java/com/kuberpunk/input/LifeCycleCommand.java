package com.kuberpunk.input;

public enum
LifeCycleCommand {

    START("/register-client"),
    STOP("/unregister-client");

    public final String subPath;

    LifeCycleCommand(String subPath) {
        this.subPath = subPath;
    }
}
