package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "/reload")
public class ReloadCommand extends BaseCommand {

    @Parameters(arity = "0..1", descriptionKey = "/reload.env", completionCandidates = EnvNames.class)
    private String env;
    
    @Option(names = "-quiet", descriptionKey = "/reload.-quiet")
    private boolean quiet;
    
    @Option(names = "-restore", descriptionKey = "/reload.-restore")
    private boolean restore;
    
    public ReloadCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }    

    @Override
    public void run() {

        if (env != null && !env.isEmpty()) {

            if (FileManager.get().getEnvNames().contains(env)) {
                commandProcessor.getSession().setEnv(env);
            } else {
                commandProcessor.getSession().getFeedback()
                .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.unknownName", env)).flush();
                return;
            }            
        }
        
        if (restore) {
            commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload.restore") + "\n").flush();
            commandProcessor.getSession().restore(quiet);
        } else {
            commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload") + "\n").flush();
            commandProcessor.getSession().reload(quiet);
        }       
    }
}
