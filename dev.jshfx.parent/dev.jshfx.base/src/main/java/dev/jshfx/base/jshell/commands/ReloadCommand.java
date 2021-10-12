package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/reload")
public class ReloadCommand extends BaseCommand {

    public ReloadCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }
    
    @Option(names = "-quiet", descriptionKey = "/reload.-quiet")
    private boolean quiet;
    
    @Option(names = "-restore", descriptionKey = "/reload.-restore")
    private boolean restore;

    @Override
    public void run() {

        if (restore) {
            commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload.restore") + "\n");
            commandProcessor.getSession().restore(quiet);
        } else {
            commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload") + "\n");
            commandProcessor.getSession().reload(quiet);
        }       
    }
}
