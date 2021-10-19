package dev.jshfx.base.jshell.commands;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Feedback;
import dev.jshfx.base.jshell.Settings;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/set")
public class SetCommand extends BaseCommand {

    @Option(names = "feedback", arity = "0..1", paramLabel = "<mode>", descriptionKey = "/set.feedback", completionCandidates = FeedbakcModes.class)
    private String feedback;

    @Option(names = "start", arity = "0..*", paramLabel = "<file>", descriptionKey = "/set.start", completionCandidates = StartOptions.class)
    private List<String> start;

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
            commandProcessor.getSession().getFeedback().commandResult(names).flush();;
        } else if (start != null && !start.isEmpty()) {
            start.forEach(f -> {
                if (Settings.PREDEFINED_STARTUP_FILES.keySet().contains(f)) {
                    commandProcessor.getSession().getSettings().getPredefinedStartupFiles().add(f);
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(true);
                } else if ("-none".equals(f)) {
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(false);
                } else if ("-all".equals(f)) {
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(true);
                } else if ("-clear".equals(f)) {
                    commandProcessor.getSession().getSettings().getPredefinedStartupFiles().clear();
                    commandProcessor.getSession().getSettings().getStartupFiles().clear();
                } else {
                    commandProcessor.getSession().getSettings().getStartupFiles().add(f);
                    commandProcessor.getSession().getSettings().setLoadStartupFiles(true);
                }
            });
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
            return List.of("-all", "-clear", "-none", Settings.DEFAULT, Settings.PRINTING).iterator();
        }
    }
}
