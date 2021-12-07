package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = ExitCommand.NAME)
public class ExitCommand extends BaseCommand {

    public static final String NAME = "/exit";

    public ExitCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        commandProcessor.getSession().exit();
    }
}
