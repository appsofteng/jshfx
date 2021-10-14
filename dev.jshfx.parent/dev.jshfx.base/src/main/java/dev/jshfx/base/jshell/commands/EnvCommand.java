package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    @Option(names = "-gui", descriptionKey = "/env.-gui")
    private boolean gui;

    public EnvCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {


    }
}
