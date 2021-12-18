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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.FileManager;
import dev.jshfx.base.ui.ConsoleModel;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jdk.jshell.execution.ObjectExecutionControlProvider;
import dev.jshfx.jfx.concurrent.QueueTask;
import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jfx.file.FXPath;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.jx.tools.GroupNames;
import dev.jshfx.jx.tools.JavaSourceResolver;
import dev.jshfx.jx.tools.Lexer;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class Session {

    public static final String PRIVILEDGED_TASK_QUEUE = "priviledged-task-queue";

    private Runnable onExitCommand = () -> {
    };
    private BiConsumer<SnippetEvent, Object> resultHandler = (e, o) -> {
    };
    private Env env;
    private Settings settings;
    private Feedback feedback;
    private Timer timer = new Timer();
    private JShell jshell;
    private TaskQueuer taskQueuer;
    private FXPath fxPath;
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
    private JavaSourceResolver javaSourceResolver;
    private ObjectExecutionControlProvider objectExecutionControlProvider;
    private Lexer lexer = Lexer.get("jsh");

    public Session(FXPath fxPath, ConsoleModel consoleModel, TaskQueuer taskQueuer) {
        this.fxPath = fxPath;
        this.consoleModel = consoleModel;
        this.taskQueuer = taskQueuer;
        objectExecutionControlProvider = new ObjectExecutionControlProvider();

        commandProcessor = new CommandProcessor(this);
        snippetProcessor = new SnippetProcessor(this);
        settings = loadSettings();
        env = loadEnv();
        javaSourceResolver = new JavaSourceResolver();
        javaSourceResolver.setResourceBundle(k -> FXResourceBundle.getBundle().getString​(k));
        setSources();
        feedback = new Feedback(consoleModel, settings);
        idGenerator = new IdGenerator();
    }

    public void init() {
        taskQueuer.add(() -> initJShell());
        loadStartupFiles();
    }

    public void setOnExitCommand(Runnable value) {
        this.onExitCommand = value;
    }

    public void setOnResult(BiConsumer<SnippetEvent, Object> resultHandler) {
        this.resultHandler = resultHandler;
    }

    public Path getCurDir() {
        return fxPath.getPath().getParent();
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public Timer getTimer() {
        return timer;
    }

    public JShell getJshell() {
        return jshell;
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

    public JavaSourceResolver getJavaSourceResolver() {
        return javaSourceResolver;
    }

    public int getStartSnippetMaxIndex() {
        return startSnippetMaxIndex;
    }

    public void setStartSnippetMaxIndex(int startSnippetMaxIndex) {
        this.startSnippetMaxIndex = startSnippetMaxIndex;
    }

    public Map<String, Snippet> getSnippetsById() {
        return snippetsById;
    }

    public Map<String, List<Snippet>> getSnippetsByName() {
        return snippetsByName;
    }

    public void setIO() {
        System.setIn(consoleModel.getIn());
        System.setErr(consoleModel.getErr());
        System.setOut(consoleModel.getOut());
    }

    public Env getEnv() {
        return env;
    }

    public void setEnv(Env env) {
        this.env = env;
    }

    public void addToClasspath(Set<String> paths) {
        env.getClassPaths().addAll(paths);
        paths.forEach(p -> {
            jshell.addToClasspath(p);
        });
    }

    public boolean setClasspath(Set<String> paths) {
        boolean change = false;

        if (!env.getClassPaths().equals(paths)) {
            env.getClassPaths().clear();
            env.getClassPaths().addAll(paths);
            change = true;
        }

        return change;
    }

    public void addToSourcepath(Set<String> paths) {
        env.getSourcePaths().addAll(paths);
        setSources();
    }

    public void setSourcepath(Set<String> paths) {
        env.getSourcePaths().clear();
        env.getSourcePaths().addAll(paths);
        setSources();
    }

    private void setSources() {
        List<Path> sources = new ArrayList<>(FileManager.get().getSourcePaths());
        env.getSourcePaths().forEach(p -> sources.add(Path.of(p)));
        javaSourceResolver.setSourcePaths(sources);
    }

    private Env loadEnv() {
        return JsonUtils.get().fromJson(FileManager.ENV_FILE, Env.class, new Env());
    }

    public void saveEnv() {
        JsonUtils.getWithFormatting().toJson(env, FileManager.ENV_FILE);
    }

    public Settings getSettings() {
        return settings;
    }

    public Settings loadSettings() {
        return JsonUtils.get().fromJson(FileManager.SET_FILE, Settings.class, new Settings());
    }

    public void saveSettings() {
        JsonUtils.getWithFormatting().toJson(settings, FileManager.SET_FILE);
    }

    private void setListener() {
        subscription = jshell.onSnippetEvent(e -> {

            if (e.snippet() == null || e.snippet().id() == null) {
                return;
            }

            if (!objectExecutionControlProvider.getExecutionControl().getResults().isEmpty()) {
                var result = objectExecutionControlProvider.getExecutionControl().getResults().remove(0);
                if (result != null) {
                    resultHandler.accept(e, result);
                }
            }

            String name = SnippetUtils.getName(e.snippet());

            snippetsById.put(e.snippet().id(), e.snippet());
            List<Snippet> snippets = snippetsByName.computeIfAbsent(name, k -> new ArrayList<>());
            snippets.add(e.snippet());
        });
    }

    public void reset() {
        setRestoreSnippets();
        restart();
    }

    public void reload(boolean quiet) {
        setRestoreSnippets();
        restart();
        reloadSnippets(quiet);
    }

    public void restore(boolean quiet) {
        restart();
        reloadSnippets(quiet);
    }

    private void restart() {
        initJShell();
        loadStartupFiles();
    }

    private void initJShell() {
        snippetsById.clear();
        snippetsByName.clear();

        buildJShell();
        setListener();

        if (settings.isLoadStartupFiles()) {
            loadPredefinedStartupFiles();
        }
    }

    private void loadStartupFiles() {
        if (settings.isLoadStartupFiles()) {
            loadCustomStartupFiles();
        }

        startSnippetMaxIndex = idGenerator.getMaxId();
    }

    private void setRestoreSnippets() {
        restoreSnippets = jshell.snippets()
                .filter(s -> Integer.parseInt(s.id()) > commandProcessor.getSession().getStartSnippetMaxIndex())
                .filter(s -> jshell.status(s) == Status.VALID || jshell.status(s) == Status.DROPPED)
                .map(s -> new SimpleEntry<>(s, jshell.status(s))).collect(Collectors.toList());
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
            String[] options = env.getOptions(FileManager.get().getClassPath(), "", "",
                    List.of("--enable-preview", "--release", FileManager.JAVA_RELEASE));

            jshell = JShell.builder().executionEngine(objectExecutionControlProvider, null).idGenerator(idGenerator)
                    .in(consoleModel.getIn()).out(consoleModel.getOut()).err(consoleModel.getErr())
                    .compilerOptions(options).remoteVMOptions(options).build();

            // Create the analysis before putting on the class path.
            jshell.sourceCodeAnalysis();

            if (env.isLoad()) {
                env.getClassPaths().forEach(p -> jshell.addToClasspath(p));
            }
            jshell.addToClasspath(FileManager.get().getClassPath());
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

        javaSourceResolver.close();
    }

    public void exit() {
        onExitCommand.run();
    }

    public void loadPredefinedStartupFile(String file) {
        try {

            snippetProcessor.process(getClass().getResourceAsStream(Settings.PREDEFINED_STARTUP_FILES.get(file)));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPredefinedStartupFiles() {
        try {

            for (String file : settings.getPredefinedStartupFiles()) {
                snippetProcessor.process(getClass().getResourceAsStream(Settings.PREDEFINED_STARTUP_FILES.get(file)));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCustomStartupFiles() {

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

        timer.start();
        history.add(input.strip());

        var tokens = lexer.tokenize(input);
        StringBuilder snippets = new StringBuilder();
        List<QueueTask<Void>> tasks = new ArrayList<>();

        tokens.stream().filter(t -> !t.getType().equals(GroupNames.COMMENT) && !t.getType().equals(GroupNames.NEWLINE))
                .forEach(token -> {
                    if (token.getType().equals(GroupNames.JSHELLCOMMAND)) {
                        if (snippets.length() > 0) {
                            tasks.add(snippetProcessor.getTask(snippets.toString()));
                            snippets.setLength(0);
                        }
                        tasks.add(commandProcessor.getTask(token.getValue()));
                    } else {
                        snippets.append(token.getValue());
                    }
                });

        if (snippets.length() > 0) {
            tasks.add(snippetProcessor.getTask(snippets.toString()));
        }

        if (!tasks.isEmpty()) {
            var task = tasks.get(tasks.size() - 1);
            task.setOnSucceeded(e -> timer.stop());
            task.setOnFailed(e -> timer.stop());
            tasks.forEach(t -> taskQueuer.add(t));
        }
    }
}
