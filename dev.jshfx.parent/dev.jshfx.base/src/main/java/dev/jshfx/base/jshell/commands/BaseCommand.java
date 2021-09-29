package dev.jshfx.base.jshell.commands;

import picocli.CommandLine.Model.CommandSpec;
import dev.jshfx.base.jshell.CommandProcessor;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

public abstract class BaseCommand  implements Runnable {

    protected CommandProcessor commandProcessor;

    @Option(names = {"-h", "--help"}, usageHelp = true, descriptionKey = "-h")
    protected boolean help;

    @Spec
    protected CommandSpec commandSpec;

    public BaseCommand(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }
    
    @Override
    public void run() {
    	commandSpec.commandLine().usage(commandSpec.commandLine().getOut());
    	
    }
}
