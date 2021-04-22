package com.kuberpunk.input;

import com.kuberpunk.strategy.ProxyLifeCycleStrategy;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kuberpunk.input.LifeCycleCommand.START;
import static com.kuberpunk.input.LifeCycleCommand.STOP;

@AllArgsConstructor
public class ArgumentParser {

    private static final Logger logger = LoggerFactory.getLogger(ArgumentParser.class);

    private final HelpFormatter formatter = new HelpFormatter();

    private final ProxyLifeCycleStrategy openShiftSidecarStrategy;

    public void parse(String... args) {
        logger.info("Kuberpunk started with arguments: {}", args);
        InputClusterArgs inputClusterArgs = new InputClusterArgs();
        Options options = new Options();
        options.addOption("h", "help", false,
                "Help to use Kuberpunk");
        options.addOption("s", "service", true,
                "The service you are going to redirect");
        options.addRequiredOption("n", "namespace",
                true, "Namespace in which you work");
        options.addOption("p", "port", true,
                "The port on your computer to which the call will be forwarded");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;


        OptionGroup optionGroup = new OptionGroup();
        optionGroup.addOption(new Option("start", "start-proxying", false, "Creates cloud controller and starts proxying"));
        optionGroup.addOption(new Option("stop", "stop-proxying", false, "Stops proxying"));
        optionGroup.addOption(new Option("create", "create-cloud-controller", false, "Creates cloud controller"));
        optionGroup.addOption(new Option("h", "help", false, "Help to use Kuberpunk"));
        options.addOptionGroup(optionGroup);
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("kuberpunk-client", options);
                return;
            }
            if (cmd.hasOption("s")) {
                logger.info("Added service : {}", cmd.getOptionValue("s"));
                inputClusterArgs.setService(cmd.getOptionValue("s"));
            }
            if (cmd.hasOption("n")) {
                logger.info("Added namespace: {}", cmd.getOptionValue("n"));
                inputClusterArgs.setNamespace(cmd.getOptionValue("n"));
            }
            if (cmd.hasOption("p")) {
                logger.info("Added port: {}", cmd.getOptionValue("p"));
                inputClusterArgs.setPort(cmd.getOptionValue("p"));
            }
            if (cmd.hasOption("start")) {
                inputClusterArgs.setCycleCommand(START);
                logger.info("Added option: {}", "START");
                openShiftSidecarStrategy.startProxying(inputClusterArgs);
            }
            if (cmd.hasOption("stop")) {
                logger.info("Added option: {}", "STOP");
                inputClusterArgs.setCycleCommand(STOP);
                inputClusterArgs.setPort(null);
                openShiftSidecarStrategy.stopProxying(inputClusterArgs);
            }
            if (cmd.hasOption("create")) {
                inputClusterArgs.setCycleCommand(START);
                openShiftSidecarStrategy.startProxying(new InputClusterArgs());
            }
        } catch (ParseException pe) {
            formatter.printHelp("kuberpunk-client", options);
        }
    }
}

