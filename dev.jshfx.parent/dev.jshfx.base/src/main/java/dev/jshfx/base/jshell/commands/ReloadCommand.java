package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;

@Command(name = "/reload")
public class ReloadCommand extends BaseCommand {

    public ReloadCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("reloadingState") + "\n");
        commandProcessor.getSession().reload();
    }
}
