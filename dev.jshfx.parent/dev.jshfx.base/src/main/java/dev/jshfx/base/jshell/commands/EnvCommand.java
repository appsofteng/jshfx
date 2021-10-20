package dev.jshfx.base.jshell.commands;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.ExportItem;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    @Option(names = "-retain", arity = "0..1", paramLabel = "<name>", descriptionKey = "/env.-retain", completionCandidates = EnvNames.class)
    private String retain;

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
            Set<String> paths = Set.copyOf(Arrays.asList(classpath.split(String.valueOf(File.pathSeparatorChar))));
            commandProcessor.getSession().getEnv().setClassPath(paths);
        }

        if (modulepath != null) {
            Set<String> paths = Set.copyOf(Arrays.asList(modulepath.split(String.valueOf(File.pathSeparatorChar))));
            commandProcessor.getSession().getEnv().setModulePath(paths);
        }

        if (addModules != null) {
            commandProcessor.getSession().getEnv().setAddModules(Set.copyOf(addModules));
        }

        if (addExports != null) {
            try {
                Set<ExportItem> exports = addExports.stream().map(ExportItem::parse).collect(Collectors.toSet());
                commandProcessor.getSession().getEnv().setAddExports(exports);
            } catch (IllegalArgumentException e) {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.env.adExports.failure.illegalArgument", e.getMessage()))
                        .flush();
            }
        }

        if (retain != null) {

            if (retain.isEmpty()) {
                commandProcessor.getSession().saveEnv();
                commandProcessor.getSession().getFeedback()
                        .commandSuccess(FXResourceBundle.getBundle().getString​("msg.env.save.success")).flush();
            } else if (retain.matches("\\w+")) {
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
    }
}
