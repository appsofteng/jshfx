package dev.jshfx.base.jshell.commands;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.sys.PreferenceManager;
import dev.jshfx.base.sys.RepositoryManager;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/resolve")
public class ResolveCommand extends BaseCommand {

    public static final String NAME = "/resolve";
    
    @Parameters(paramLabel = "<artifacts>", descriptionKey = "/resolve.artifacts", completionCandidates = RepoCoordinates.class)
    private List<String> coords;

    public ResolveCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (coords != null && !coords.isEmpty()) {
            commandProcessor.getSession().getFeedback()
            .commandSuccess(FXResourceBundle.getBundle().getString​("msg.resolution.started")).flush();
            try {
                Set<String> classPaths = new HashSet<>();
                Set<String> sourcePaths = new HashSet<>();

                for (String coord : coords) {
                    if (coord.endsWith(".xml")) {
                        Path path = Path.of(coord);
                        if (!path.isAbsolute()) {
                            path = PreferenceManager.get().getLatestDir().resolve(path);
                        }

                        RepositoryManager.get().resolvePom(path.toString(), classPaths, sourcePaths);
                    } else {
                        RepositoryManager.get().resolve(coord, classPaths, sourcePaths);
                    }
                }

                commandProcessor.getSession().getFeedback()
                        .commandSuccess(FXResourceBundle.getBundle().getString​("msg.resolution.success")).flush();

                commandProcessor.getSession().setSourcepath(sourcePaths);
                
                if (commandProcessor.getSession().setClasspath(classPaths)) {
                    commandProcessor.getSession().getFeedback()
                    .commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload")).flush();
                    commandProcessor.getSession().reload(true);
                    commandProcessor.getSession().getFeedback()
                    .commandSuccess(FXResourceBundle.getBundle().getString​("msg.reload.done")).flush();
                }

            } catch (Exception e) {
                commandProcessor
                        .getSession().getFeedback().commandFailure(FXResourceBundle.getBundle()
                                .getString​("msg.resolution.failure", e.getMessage() != null ? e.getMessage() : coords))
                        .flush();
            }
        }
    }
}
