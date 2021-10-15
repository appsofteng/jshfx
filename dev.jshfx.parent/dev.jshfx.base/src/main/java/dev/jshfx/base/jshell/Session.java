package dev.jshfx.base.jshell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.FileManager;
import dev.jshfx.base.sys.PreferenceManager;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jfx.scene.control.ConsoleModel;
import dev.jshfx.jfx.scene.control.SplitConsolePane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Window;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.execution.LocalExecutionControlProvider;

public class Session {

    public static final String PRIVILEDGED_TASK_QUEUE = "priviledged-task-queue";

    private static JShell commonJshell = JShell.builder().executionEngine(new LocalExecutionControlProvider(), null)
            .build();

    private BooleanProperty closed = new SimpleBooleanProperty();
    private Env env;
    private Settings settings;
    private Feedback feedback;
    private JShell jshell;
    private SplitConsolePane console;
    private TaskQueuer taskQueuer;
    private ConsoleModel consoleModel;
    private List<String> history = new ArrayList<>();
    private IdGenerator idGenerator;
    private CommandProcessor commandProcessor;
    private SnippetProcessor snippetProcessor;
    private int startSnippetMaxIndex;
    private Map<String, Snippet> snippetsById = new HashMap<>();
    private Map<String, List<Snippet>> snippetsByName = new HashMap<>();
    private List<Entry<Snippet, Status>> restoreSnippets = new ArrayList<>();
    private Subscription subscription;

    public Session(SplitConsolePane console, TaskQueuer taskQueuer) {

        this.console = console;
        this.consoleModel = console.getConsoleModel();
        this.taskQueuer = taskQueuer;

        commandProcessor = new CommandProcessor(this);
        snippetProcessor = new SnippetProcessor(this);
        settings = loadSettings();
        env = loadEnv();
        feedback = new Feedback(consoleModel, settings);
        idGenerator = new IdGenerator();
        restart();
        setListener();
    }

    public static List<Documentation> documentation(String input, int cursor, boolean computeJavadoc) {
        return commonJshell.sourceCodeAnalysis().documentation(input, cursor, computeJavadoc);
    }

    public static void closeCommon() {

        if (commonJshell != null) {

            commonJshell.stop();
            commonJshell.close();
        }
    }

    public ReadOnlyBooleanProperty closedProperty() {
        return closed;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public JShell getJshell() {
        return jshell;
    }

    SplitConsolePane getConsoleView() {
        return console;
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public TaskQueuer getTaskQueuer() {
        return taskQueuer;
    }

    public List<String> getHistory() {
        return history;
    }

    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    public SnippetProcessor getSnippetProcessor() {
        return snippetProcessor;
    }

    public int getStartSnippetMaxIndex() {
        return startSnippetMaxIndex;
    }

    public void setStartSnippetMaxIndex(int startSnippetMaxIndex) {
        this.startSnippetMaxIndex = startSnippetMaxIndex;
    }

    public Window getWindow() {
        return console.getScene().getWindow();
    }

    public Map<String, Snippet> getSnippetsById() {
        return snippetsById;
    }

    public Map<String, List<Snippet>> getSnippetsByName() {
        return snippetsByName;
    }

    public void setIO() {
        System.setErr(consoleModel.getErr());
        System.setOut(consoleModel.getOut());
    }

    public Env getEnv() {
        return env;
    }

    public void addToClasspath(List<String> paths) {
        env.getClassPath().addAll(paths);
        paths.forEach(p -> jshell.addToClasspath(p));
    }

    public Env loadEnv() {
        return JsonUtils.get().fromJson(FileManager.get().getEnvFile(PreferenceManager.get().getEnv()), Env.class,
                new Env(PreferenceManager.DEFAULT_ENV_NAME));
    }

    public void saveEnv() {
        PreferenceManager.get().setEnv(env.getName());
        JsonUtils.get().toJson(env, FileManager.get().getEnvFile(env.getName()));
    }

    public Settings getSettings() {
        return settings;
    }

    public Settings loadSettings() {
        return JsonUtils.getWithFormatting().fromJson(FileManager.SET_FILE, Settings.class, new Settings());
    }

    public void saveSettings() {
        JsonUtils.get().toJson(settings, FileManager.SET_FILE);
    }

    private void setListener() {
        subscription = jshell.onSnippetEvent(e -> {

            if (e.snippet() == null || e.snippet().id() == null) {
                return;
            }

            String name = SnippetUtils.getName(e.snippet());

            snippetsById.put(e.snippet().id(), e.snippet());
            List<Snippet> snippets = snippetsByName.computeIfAbsent(name, k -> new ArrayList<>());
            snippets.add(e.snippet());
        });
    }

    public void reset() {
        restoreSnippets = jshell.snippets()
                .filter(s -> Integer.parseInt(s.id()) > commandProcessor.getSession().getStartSnippetMaxIndex())
                .filter(s -> jshell.status(s) == Status.VALID || jshell.status(s) == Status.DROPPED)
                .map(s -> new SimpleEntry<>(s, jshell.status(s))).collect(Collectors.toList());
        restart();
    }

    private void restart() {
        snippetsById.clear();
        snippetsByName.clear();

        buildJShell();
        setListener();

        if (settings.isLoadStartupFiles()) {
            loadPredefinedStartupFiles();
            loadStartupFiles();
        }

        startSnippetMaxIndex = idGenerator.getMaxId();
    }

    public void reload(boolean quiet) {
        reset();
        reloadSnippets(quiet);
    }

    public void restore(boolean quiet) {
        restart();
        reloadSnippets(quiet);
    }

    private void reloadSnippets(boolean quiet) {
        restoreSnippets.forEach(s -> {
            var newSnippets = snippetProcessor.process(s.getKey(), quiet).stream().map(SnippetEvent::snippet)
                    .collect(Collectors.toList());
            if (s.getValue() == Status.DROPPED) {
                commandProcessor.drop(newSnippets);
            }
        });
    }

    private void buildJShell() {

        close();
        try {
            String[] options = env.getOptions();
            jshell = JShell.builder().executionEngine(new LocalExecutionControlProvider(), null)
                    .idGenerator(idGenerator).in(consoleModel.getIn()).out(consoleModel.getOut())
                    .err(consoleModel.getErr()).compilerOptions(options).remoteVMOptions(options).build();
            // Create the analysis before putting on the class path.
            jshell.sourceCodeAnalysis();
            env.getClassPath().forEach(p -> jshell.addToClasspath(p));
            env.getModuleLocations().forEach(p -> jshell.addToClasspath(p));
            idGenerator.setJshell(jshell);

        } catch (Exception e) {
            e.printStackTrace(consoleModel.getErr());
        }
    }

    public void stop() {
        jshell.stop();
    }

    public void close() {

        if (jshell != null) {
            if (subscription != null) {
                jshell.unsubscribe(subscription);
            }

            jshell.stop();
            jshell.close();
        }
    }

    public void exit() {

        close();
        closed.set(true);
    }

    public void loadPredefinedStartupFile(String file) {
        try {

            JShellUtils.loadSnippets(jshell,
                    getClass().getResourceAsStream(Settings.PREDEFINED_STARTUP_FILES.get(file)));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPredefinedStartupFiles() {
        try {

            for (String file : settings.getPredefinedStartupFiles()) {
                JShellUtils.loadSnippets(jshell,
                        getClass().getResourceAsStream(Settings.PREDEFINED_STARTUP_FILES.get(file)));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadStartupFiles() {

        for (String file : settings.getStartupFiles()) {
            Path path = Path.of(file);
            if (Files.exists(path)) {
                try {
                    String spippets = Files.readString(path);
                    process(spippets);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void process(String input) {

        if (input.isBlank()) {
            return;
        }

        history.add(input.strip());

        String[] lines = input.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {

            if (CommandProcessor.isCommand(line)) {
                if (sb.length() > 0) {
                    String snippets = sb.toString();
                    snippetProcessor.process(snippets);
                    sb.delete(0, sb.length());
                }
                commandProcessor.process(line);
            } else {
                sb.append(line).append("\n");
            }
        }

        if (sb.length() > 0) {
            snippetProcessor.process(sb.toString());
        }
    }
}
