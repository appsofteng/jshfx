package dev.jshfx.base.jshell.commands;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;

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
                List<Artifact> artifacts = RepositoryManager.get().resolve(coords);

                artifacts.forEach(artifact -> System.out.println(artifact + " resolved to  " + artifact.getFile()));
                
            } catch (ArtifactResolutionException e) {
                commandProcessor.getSession().getFeedback().commandFailure(FXResourceBundle.getBundle().getString​("msg.resolution.failure", coords));
            }
        }
    }
}
