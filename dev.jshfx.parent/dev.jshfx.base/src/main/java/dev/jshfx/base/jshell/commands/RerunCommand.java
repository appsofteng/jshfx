package dev.jshfx.base.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.util.FXResourceBundle;
import jdk.jshell.Snippet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = RerunCommand.RERUN_COMMAND)
public class RerunCommand extends BaseCommand {

    static final String RERUN_COMMAND = "/rerun";

    @Parameters(arity = "1..*", paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]")
    private ArrayList<String> parameters;

    public RerunCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    public static void setIfMatches(List<String> args) {

        if (args.get(0).matches("/(\\d+|\\d+-\\d+)( (\\d+|\\d+-\\d+|\\w+))*")) {            
            args.set(0, args.get(0).substring(1));
            args.add(0, RERUN_COMMAND);
        }
    }

    @Override
    public void run() {

        List<Snippet> snippets = commandProcessor.matches(parameters);

        if (snippets.isEmpty()) {
            commandProcessor.getSession().getFeedback()
                    .commandFailure(FXResourceBundle.getBundle().getStringâ€‹("noSuchSnippet") + "\n");
        } else {

            commandProcessor.getSession().getSnippetProcessor().process(snippets);
        }
    }
}
