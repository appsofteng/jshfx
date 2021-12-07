package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = StopCommand.NAME)
public class StopCommand extends BaseCommand {

    public static final String NAME = "/stop";

    public StopCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        commandProcessor.getSession().stop();
    }
}
