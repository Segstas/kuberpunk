package com.kuberpunk.strategy.chain;

import com.kuberpunk.hostextraction.RedirectInformationPuller;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

@AllArgsConstructor
public class ConfigCreator implements SubstitutionStrategy {

    @Nullable
    private SubstitutionStrategy next;

    private final RedirectInformationPuller redirectInformationPuller;

    public ConfigCreator(RedirectInformationPuller redirectInformationPuller) {
        this.redirectInformationPuller = redirectInformationPuller;
    }

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        redirectInformationPuller.pullRedirectInformation(inputClusterArgs.getService(), inputClusterArgs.getNamespace());
        if (next != null) {
            next.apply(inputClusterArgs);
        }
    }
}
