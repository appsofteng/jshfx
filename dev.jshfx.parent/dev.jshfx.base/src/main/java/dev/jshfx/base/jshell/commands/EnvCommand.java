package dev.jshfx.base.jshell.commands;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Env;
import dev.jshfx.base.jshell.ExportItem;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {
    
    private static final String ENV_ENAME_PATTERN = "\\w+";

    @Parameters(arity = "0..*", descriptionKey = "/env.names", completionCandidates = EnvNames.class)
    private Set<String> names;

    @Option(names = "-retain", arity = "0..1", paramLabel = "<name>", descriptionKey = "/env.-retain", completionCandidates = EnvNames.class)
    private String retain;

    @Option(names = "-delete", arity = "0..*", paramLabel = "<names>", descriptionKey = "/env.-delete", completionCandidates = EnvNames.class)
    private Set<String> delete;

    @Option(names = "-new", arity = "0..1", descriptionKey = "/env.-new")
    private String newEnv;

    @Option(names = "--class-path", arity = "0..1", paramLabel = "<path>", descriptionKey = "/env.--class-path")
    private String classpath;

    @Option(names = "--module-path", arity = "0..1", paramLabel = "<path>", descriptionKey = "/env.--module-path")
    private String modulepath;

    @Option(names = "--add-modules", arity = "0..*", paramLabel = "<module>", split = ",", descriptionKey = "/env.--add-modules")
    private List<String> addModules;

    @Option(names = "--add-exports", arity = "0..*", paramLabel = "<module>/<package>=<target-module>", descriptionKey = "/env.--add-exports")
    private List<String> addExports;

    public EnvCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (names != null && !names.isEmpty()) {
            var diff = new HashSet<>(names);
            diff.removeAll(FileManager.get().getEnvNames());
            if (diff.isEmpty()) {

                String envs = commandProcessor.getSession().getEnvs(names).stream().map(Env::toString)
                        .collect(Collectors.joining("\n", "", "\n"));
                commandProcessor.getSession().getFeedback().commandResult(envs).flush();
            } else {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.unknownName", diff)).flush();
            }
            return;
        }

        if (delete != null) {

            var diff = new HashSet<>(delete);
            diff.removeAll(FileManager.get().getEnvNames());
            if (diff.isEmpty()) {
                try {
                    commandProcessor.getSession().deleteEnvs(delete);
                    commandProcessor.getSession().getFeedback()
                            .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.delete.success")).flush();
                } catch (Exception e) {
                    commandProcessor.getSession().getFeedback()
                            .commandFailure(
                                    FXResourceBundle.getBundle().getString​("msg.env.delete.failure", e.getMessage()))
                            .flush();
                }
            } else {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.unknownName", diff)).flush();
            }

            return;
        }

        if (newEnv != null) {
            
            if (newEnv.isEmpty()) {
                newEnv = commandProcessor.getSession().getNewEnvName();
            } else if (!newEnv.matches(ENV_ENAME_PATTERN)) {
                commandProcessor.getSession().getFeedback()
                .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.save.failure.invalidName"))
                .flush();
                
                return;
            }
            
            commandProcessor.getSession().setEnv(new Env(newEnv));
        }

        if (classpath != null) {
            Collection<String> paths = classpath.isEmpty() ? Set.of()
                    : Arrays.asList(classpath.split(String.valueOf(File.pathSeparatorChar)));
            commandProcessor.getSession().getEnv().getClassPaths().clear();
            commandProcessor.getSession().getEnv().getClassPaths().addAll(paths);
        }

        if (modulepath != null) {
            Collection<String> paths = modulepath.isEmpty() ? Set.of()
                    : Arrays.asList(modulepath.split(String.valueOf(File.pathSeparatorChar)));
            commandProcessor.getSession().getEnv().getModulePaths().clear();
            commandProcessor.getSession().getEnv().getModulePaths().addAll(paths);
        }

        if (addModules != null) {
            commandProcessor.getSession().getEnv().getAddModules().clear();
            commandProcessor.getSession().getEnv().getAddModules().addAll(addModules);
        }

        if (addExports != null) {
            try {
                Set<ExportItem> exports = addExports.stream().map(ExportItem::parse).collect(Collectors.toSet());
                commandProcessor.getSession().getEnv().getAddExports().clear();
                commandProcessor.getSession().getEnv().getAddExports().addAll(exports);
            } catch (IllegalArgumentException e) {
                commandProcessor.getSession().getFeedback().commandFailure(FXResourceBundle.getBundle()
                        .getString​("msg.env.adExports.failure.illegalArgument", e.getMessage())).flush();
            }
        }

        if (classpath != null || modulepath != null || addModules != null || addExports != null) {
            commandProcessor.getSession().reload(true);

        }

        if (retain != null) {

            if (retain.isEmpty()) {
                commandProcessor.getSession().saveEnv();
                commandProcessor.getSession().getFeedback()
                        .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.save.success")).flush();
            } else if (retain.matches(ENV_ENAME_PATTERN)) {
                commandProcessor.getSession().getEnv().setName(retain);
                commandProcessor.getSession().saveEnv();
                commandProcessor.getSession().getFeedback()
                        .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.save.success")).flush();
            } else {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.save.failure.invalidName"))
                        .flush();
            }
        }

        if (classpath == null && modulepath == null && addModules == null && addExports == null && retain == null && newEnv == null) {

            String envs = commandProcessor.getSession().getEnvs().stream().map(Env::toString)
                    .collect(Collectors.joining("\n", "", "\n"));
            commandProcessor.getSession().getFeedback().commandResult(envs).flush();
        }
    }
}
