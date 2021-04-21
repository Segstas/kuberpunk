package com.kuberpunk.input;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ArgumentParser {

    public InputClusterArgs parse(String... args) {
        InputClusterArgs inputClusterArgs = new InputClusterArgs();
        Options options = new Options();
        options.addOption("s", "service", true,
                "The service you are going to redirect");
        options.addRequiredOption("n", "namespace",
                false, "Namespace in which you work");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("s")) {
                inputClusterArgs.setService(cmd.getOptionValue("c"));
            }
            if (cmd.hasOption("n")) {
                inputClusterArgs.setNamespace(cmd.getOptionValue("n"));
            }
        } catch (ParseException pe) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Kuberpunk", options);
        }
        return inputClusterArgs;
    }
}

