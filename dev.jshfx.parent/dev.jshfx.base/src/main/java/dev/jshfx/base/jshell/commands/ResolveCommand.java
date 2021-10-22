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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "/resolve")
public class ResolveCommand extends BaseCommand {

    @Parameters(paramLabel = "<artifacts>", descriptionKey = "/resolve.artifacts")
    private List<String> coords;

    @Option(names = "-set", descriptionKey = "/resolve.-set")
    private boolean set;

    public ResolveCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (coords != null && !coords.isEmpty()) {

            try {
                Set<String> artifacts = new HashSet<>();

                for (String coord : coords) {
                    if (coord.endsWith(".xml")) {
                        Path path = Path.of(coord);
                        if (!path.isAbsolute()) {
                            path = PreferenceManager.get().getLatestDir().resolve(path);
                        }
                        
                        RepositoryManager.get().resolvePom(path.toString(), artifacts);
                    } else {
                        RepositoryManager.get().resolve(coord, artifacts);
                    }
                }

                commandProcessor.getSession().getFeedback()
                        .commandSuccess(FXResourceBundle.getBundle().getString​("msg.resolution.success")).flush();

                if (set) {
                    commandProcessor.getSession().getEnv().getClassPaths().clear();
                    commandProcessor.getSession().getEnv().getClassPaths().addAll(artifacts);
                    commandProcessor.getSession().reload(true);
                } else {
                    commandProcessor.getSession().addToClasspath(artifacts);
                }

            } catch (Exception e) {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.resolution.failure", e.getMessage() != null ? e.getMessage() : coords))
                        .flush();
            }
        }
    }
}
