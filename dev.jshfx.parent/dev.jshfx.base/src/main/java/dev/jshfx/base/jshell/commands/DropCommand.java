package dev.jshfx.base.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.SnippetUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/drop")
public class DropCommand extends BaseCommand {

    @Parameters(arity = "1..*", paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]")
    private ArrayList<String> parameters;

    public DropCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        drop(commandProcessor.matches(parameters));
    }

    public void drop(List<Snippet> snippets) {
        StringBuilder sb = new StringBuilder();
        snippets.forEach(s -> {
            if (commandProcessor.getSession().getJshell().status(s) == Status.VALID) {
                commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("dropped")
                        + SnippetUtils.toString(s, commandProcessor.getSession().getJshell()));
                commandProcessor.getSession().getJshell().drop(s);
            } else {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("notValid")
                                + SnippetUtils.toString(s, commandProcessor.getSession().getJshell())).flush();
            }
        });

        if (sb.length() == 0) {
            commandProcessor.getSession().getFeedback()
                    .commandFailure(FXResourceBundle.getBundle().getString​("noSuchSnippet") + "\n").flush();
        }
    }
}
