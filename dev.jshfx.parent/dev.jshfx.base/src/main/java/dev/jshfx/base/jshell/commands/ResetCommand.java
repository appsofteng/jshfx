package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.scene.control.ConsoleModel;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;

@Command(name = "/reset")
public class ResetCommand extends BaseCommand {

    public ResetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        commandProcessor.getSession().getFeedback().normaln(FXResourceBundle.getBundle().getString​("resetingState"), ConsoleModel.COMMENT_STYLE);
        commandProcessor.getSession().reset();
    }
}
