package dev.jshfx.base.jshell.commands;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.ExportItem;
import dev.jshfx.j.nio.file.PathUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    @Option(names = "-retain", descriptionKey = "/env.-retain")
    private boolean retain;

    @Option(names = "start", arity = "0..*", paramLabel = "<options>", descriptionKey = "/env.start", completionCandidates = StartOptions.class)
    private List<String> start;

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

        if (classpath != null) {
            Collection<String> paths = PathUtils.split(classpath);
            commandProcessor.getSession().getEnv().getClassPaths().clear();
            commandProcessor.getSession().getEnv().getClassPaths().addAll(paths);
            commandProcessor.getSession().getEnv().setLoad(true);
        }

        if (modulepath != null) {
            Collection<String> paths = PathUtils.split(modulepath);
            commandProcessor.getSession().getEnv().getModulePaths().clear();
            commandProcessor.getSession().getEnv().getModulePaths().addAll(paths);
            commandProcessor.getSession().getEnv().setLoad(true);
        }

        if (addModules != null) {
            commandProcessor.getSession().getEnv().getAddModules().clear();
            commandProcessor.getSession().getEnv().getAddModules().addAll(addModules);
            commandProcessor.getSession().getEnv().setLoad(true);
        }

        if (addExports != null) {
            try {
                Set<ExportItem> exports = addExports.stream().map(ExportItem::parse).collect(Collectors.toSet());
                commandProcessor.getSession().getEnv().getAddExports().clear();
                commandProcessor.getSession().getEnv().getAddExports().addAll(exports);
                commandProcessor.getSession().getEnv().setLoad(true);
            } catch (IllegalArgumentException e) {
                commandProcessor.getSession().getFeedback().commandFailure(FXResourceBundle.getBundle()
                        .getString​("msg.env.adExports.failure.illegalArgument", e.getMessage())).flush();
            }
        }

        if (start != null && !start.isEmpty()) {
            start.forEach(f -> {
                if (SetCommand.NONE.equals(f)) {
                    commandProcessor.getSession().getEnv().setLoad(false);
                } else if (SetCommand.ALL.equals(f)) {
                    commandProcessor.getSession().getEnv().setLoad(true);
                } else if (SetCommand.CLEAR.equals(f)) {
                    commandProcessor.getSession().getEnv().clear();
                }
            });
        }

        if (retain) {

            commandProcessor.getSession().saveEnv();
            commandProcessor.getSession().getFeedback()
                    .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.save.success")).flush();
        }

        if (classpath == null && modulepath == null && addModules == null && addExports == null) {

            String envs = commandProcessor.getSession().getEnv().toString();
            
            if (envs.isBlank()) {
                envs = FXResourceBundle.getBundle().getString​("msg.env.empty");
            }
            
            commandProcessor.getSession().getFeedback().commandResult(envs).flush();
        } else {
            commandProcessor.getSession().reload(true);
        }
    }

    public static class StartOptions implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return SetCommand.START_OPTIONS.iterator();
        }
    }
}
