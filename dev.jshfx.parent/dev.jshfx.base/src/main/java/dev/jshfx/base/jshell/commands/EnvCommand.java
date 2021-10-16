package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    @Option(names = "-retain", arity = "0..1", descriptionKey = "/env.-retain", completionCandidates = EnvNames.class)
    private String retain;

    public EnvCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (retain != null) {

            if (retain.isEmpty()) {
                commandProcessor.getSession().saveEnv();
                commandProcessor.getSession().getFeedback()
                .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.save.success")).flush();
            } else if (retain.matches("\\w+")) {
                commandProcessor.getSession().getEnv().setName(retain);
                commandProcessor.getSession().saveEnv();
                commandProcessor.getSession().getFeedback()
                .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.save.success")).flush();
            } else {
                commandProcessor.getSession().getFeedback()
                .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.save.failure.invalidName")).flush();
            }
        }
    }
}
