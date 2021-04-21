package com.kuberpunk.strategy;

import com.kuberpunk.input.InputClusterArgs;

public interface SubstitutionStrategy {
    void apply(InputClusterArgs inputClusterArgs);
}