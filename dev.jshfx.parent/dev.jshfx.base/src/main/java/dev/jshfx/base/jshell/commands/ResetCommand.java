package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/reset")
public class ResetCommand extends BaseCommand {

    @Parameters(arity = "0..1", descriptionKey = "/reset.env", completionCandidates = EnvNames.class)
    private String env;

    public ResetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        commandProcessor.getSession().getFeedback()
                .commandSuccess(FXResourceBundle.getBundle().getString​("resetingState") + "\n").flush();

        if (env != null && !env.isEmpty()) {

            if (FileManager.get().getEnvNames().contains(env)) {
                commandProcessor.getSession().setEnv(env);
            } else {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.unknownName", env)).flush();
                return;
            }

        }

        commandProcessor.getSession().reset();
    }
}
