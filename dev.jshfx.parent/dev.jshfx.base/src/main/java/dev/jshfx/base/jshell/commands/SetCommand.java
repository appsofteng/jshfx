package dev.jshfx.base.jshell.commands;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Feedback;
import dev.jshfx.base.jshell.Settings;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/set")
public class SetCommand extends BaseCommand {
    static final String ALL = "-all";
    static final String CLEAR = "-clear";
    static final String NONE = "-none";
    static final List<String> START_OPTIONS = List.of(ALL, CLEAR, NONE);

    @Option(names = "feedback", arity = "0..1", paramLabel = "<mode>", descriptionKey = "/set.feedback", completionCandidates = FeedbakcModes.class)
    private String feedback;

    @Option(names = "start", arity = "0..*", paramLabel = "<file>", descriptionKey = "/set.start", completionCandidates = StartOptions.class)
    private List<String> start;
    
    @Option(names = "-jshpath", arity = "0..1", paramLabel = "<path>", descriptionKey = "/set.-jshpath")
    private String jshpath;

    @Option(names = "-retain", descriptionKey = "/set.-retain")
    private boolean retain;

    public SetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (feedback != null && feedback.isEmpty()) {
            String mode = commandProcessor.getSession().getSettings().getFeedbackMode();
            commandProcessor.getSession().getFeedback()
                    .commandResult(FXResourceBundle.getBundle().getString​("msg.feedback.currentMode", mode)).flush();
        } else if (feedback != null && !feedback.isEmpty()) {
            if (commandProcessor.getSession().getFeedback().isValid(feedback)) {
                commandProcessor.getSession().getSettings().setFeedbackMode(feedback);
                commandProcessor.getSession().getFeedback()
                        .commandSuccess(FXResourceBundle.getBundle().getString​("msg.feedback.mode", feedback)).flush();
            } else {
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.feedback.invalidMode", feedback));
                commandProcessor.getSession().getFeedback()
                        .commandFailure(FXResourceBundle.getBundle().getString​("msg.feedback.availableModes",
                                Feedback.MODES.stream().collect(Collectors.joining(", "))))
                        .flush();
            }
        }

        if (start != null && start.isEmpty()) {
            String names = commandProcessor.getSession().getSettings().getPredefinedStartupFiles().stream()
                    .collect(Collectors.joining("\n"));
            names += commandProcessor.getSession().getSettings().getStartupFiles().stream()
                    .collect(Collectors.joining("\n"));
            commandProcessor.getSession().getFeedback().commandResult(names).flush();
        } else if (start != null && !start.isEmpty()) {
            start.forEach(f -> {
                if (Settings.PREDEFINED_STARTUP_FILES.keySet().contains(f)) {
                    commandProcessor.getSession().getSettings().getPredefinedStartupFiles().add(f);
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(true);
                } else if (NONE.equals(f)) {
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(false);
                } else if (ALL.equals(f)) {
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(true);
                } else if (CLEAR.equals(f)) {
                    commandProcessor.getSession().getSettings().getPredefinedStartupFiles().clear();
                    commandProcessor.getSession().getSettings().getStartupFiles().clear();
                } else {
                    commandProcessor.getSession().getSettings().getStartupFiles().add(f);
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(true);
                }
            });
        }
        
        if (jshpath != null) {
            Collection<String> paths = jshpath.isEmpty() ? Set.of()
                    : Arrays.asList(jshpath.split(String.valueOf(File.pathSeparatorChar)));
            commandProcessor.getSession().getSettings().getJshPaths().clear();
            commandProcessor.getSession().getSettings().getJshPaths().addAll(paths);
        } else {
            commandProcessor.getSession().getFeedback().commandResult(commandProcessor.getSession().getSettings().getJshPaths().toString()).flush();
        }

        if (retain) {
            commandProcessor.getSession().saveSettings();
        }
    }

    public static class FeedbakcModes implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return Feedback.MODES.iterator();
        }
    }

    public static class StartOptions implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return List.of(ALL, CLEAR, NONE, Settings.DEFAULT, Settings.PRINTING).iterator();
        }
    }
}
