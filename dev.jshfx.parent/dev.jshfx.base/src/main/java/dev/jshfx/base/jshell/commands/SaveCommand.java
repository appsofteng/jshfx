package dev.jshfx.base.jshell.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Session;
import dev.jshfx.j.nio.file.PathUtils;
import dev.jshfx.j.util.LU;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import jdk.jshell.Snippet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "/save")
public class SaveCommand extends BaseCommand {

    @Parameters(paramLabel = "id", descriptionKey = "/save.ids")
    private ArrayList<String> parameters;

    @Option(names = "-all", descriptionKey = "/save.-all")
    private boolean all;

    @Option(names = "-start", descriptionKey = "/save.-start")
    private boolean start;

    @Option(names = "-history", descriptionKey = "/save.-history")
    private boolean history;

    public SaveCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (Stream.of(parameters != null && parameters.size() > 1, all, start, history).filter(o -> o).count() > 1) {
            commandProcessor.getCommandLine().getErr()
                    .println(FXResourceBundle.getBundle().getString​("onlyOneOptionAllowed") + "\n");
            return;
        }

        if (parameters == null || parameters.isEmpty()) {
            commandProcessor.getCommandLine().getErr()
                    .println(FXResourceBundle.getBundle().getString​("fileNameNotSpecified") + "\n");
            return;
        }

        String file = parameters.remove(parameters.size() - 1);

        if (all) {
            save(file, commandProcessor.getSession().getJshell().snippets().map(Snippet::source));
        } else if (start) {
            save(file,
                    commandProcessor.getSession().getJshell().snippets().filter(
                            s -> Integer.parseInt(s.id()) <= commandProcessor.getSession().getStartSnippetMaxIndex())
                            .map(Snippet::source));
        } else if (history) {
            save(file, commandProcessor.getSession().getHistory().stream());
        } else if (parameters.size() > 0) {
            save(file, commandProcessor.matches(parameters).stream().map(Snippet::source));

        } else {
            save(file, commandProcessor.getSession().getJshell().snippets()
                    .filter(s -> commandProcessor.getSession().getJshell().status(s).isActive()).map(Snippet::source));
        }
    }

    private void save(String file, Stream<String> snippets) {
        Platform.runLater(() -> {
            Path path = PathUtils.resolve(commandProcessor.getSession().getCurDir(),
                    commandProcessor.getSession().getSettings().getJshPaths(), file);
            commandProcessor.getSession().getTaskQueuer().add(Session.PRIVILEDGED_TASK_QUEUE, () -> {
                try (var f = Files.newBufferedWriter(path)) {
                    snippets.forEach(s -> LU.of(() -> {
                        f.append(s.strip());
                        f.newLine();
                    }));
                }
            });
        });
    }
}
