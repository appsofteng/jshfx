package dev.jshfx.base.jshell.commands;

import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/imports")
public class ImportCommand extends BaseCommand {

    public ImportCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);

    }

    @Override
    public void run() {
        String imports = commandProcessor.getSession().getJshell().imports().map(SnippetUtils::toString).sorted().collect(Collectors.joining("\n"));

        commandProcessor.getSession().getFeedback().normaln(imports);
    }
}
