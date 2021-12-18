package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/reload")
public class ReloadCommand extends BaseCommand {
    
    @Option(names = "-quiet", descriptionKey = "/reload.-quiet")
    private boolean quiet;
    
    @Option(names = "-restore", descriptionKey = "/reload.-restore")
    private boolean restore;
    
    public ReloadCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }    

    @Override
    public void run() {
        
        if (restore) {
            commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload.restore") + "\n").flush();
            commandProcessor.getSession().restore(quiet);
        } else {
            commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload") + "\n").flush();
            commandProcessor.getSession().reload(quiet);
        }       
    }
}
