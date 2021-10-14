package dev.jshfx.base.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.sys.RepositoryManager;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/resolve")
public class ResolveCommand extends BaseCommand {

    @Parameters(paramLabel = "artifacts", descriptionKey = "/open.artifacts")
    private List<String> coords;

    public ResolveCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (coords != null && !coords.isEmpty()) {
            
            try {
                List<String> artifacts = new ArrayList<>();
                
                for (String coord : coords) {
                    if (coord.endsWith(".xml")) {
                        RepositoryManager.get().resolvePom(coord, artifacts);
                    } else {
                        RepositoryManager.get().resolve(coord, artifacts);
                    }
                }

                commandProcessor.getSession().getFeedback().commandSuccess(FXResourceBundle.getBundle().getString​("msg.resolution.success")).flush();
                
                commandProcessor.getSession().addToClasspath(artifacts);
                
            } catch (Exception e) {
                commandProcessor.getSession().getFeedback().commandFailure(FXResourceBundle.getBundle().getString​("msg.resolution.failure", coords)).flush();
            }
        }
    }
}
