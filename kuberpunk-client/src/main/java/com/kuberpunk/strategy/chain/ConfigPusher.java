package com.kuberpunk.strategy.chain;

import com.kuberpunk.hostextraction.RedirectInformationPusher;
import com.kuberpunk.input.InputClusterArgs;
import com.kuberpunk.strategy.SubstitutionStrategy;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

@AllArgsConstructor
public class ConfigPusher implements SubstitutionStrategy {

    @Nullable
    private SubstitutionStrategy next;

    private final RedirectInformationPusher redirectInformationPusher;

    public ConfigPusher(RedirectInformationPusher redirectInformationPusher, SubstitutionStrategy substitutionStrategy) {
        this.redirectInformationPusher = redirectInformationPusher;
        this.next = substitutionStrategy;
    }

    @Override
    public void apply(InputClusterArgs inputClusterArgs) {
        if (inputClusterArgs.getService() != null) ///todo it is not obvious that if there is no service then it does not redirect
        {
            redirectInformationPusher.pushRedirectInformation(inputClusterArgs);
        }
        if (next != null) {
            next.apply(inputClusterArgs);
        }
    }
}
