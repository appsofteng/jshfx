package dev.jshfx.base.jshell.commands;

import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/history")
public class HistoryCommand extends BaseCommand {

    public HistoryCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        String result = commandProcessor.getSession().getHistory().stream().collect(Collectors.joining("\n"));

        commandProcessor.getSession().getFeedback().commandResult(result + "\n");
    }
}
