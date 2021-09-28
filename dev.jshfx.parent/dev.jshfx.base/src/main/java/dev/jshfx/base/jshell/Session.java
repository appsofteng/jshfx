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

import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jfx.scene.control.ConsoleModel;
import dev.jshfx.jfx.scene.control.SplitConsolePane;
import dev.jshfx.base.jshell.Feedback.Mode;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.j.util.json.JsonUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Window;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class Session {

	public static final String PRIVILEDGED_TASK_QUEUE = "priviledged-task-queue";

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
	private Subscription subscription;

	public Session(SplitConsolePane console, TaskQueuer taskQueuer) {

		this.console = console;
		this.consoleModel = console.getConsoleModel();
		this.taskQueuer = taskQueuer;

		feedback = new Feedback(consoleModel);
		commandProcessor = new CommandProcessor(this);
		snippetProcessor = new SnippetProcessor(this);
		env = loadEnv();
		settings = loadSettings();
		idGenerator = new IdGenerator();
		reset();
		setListener();
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

	public Env loadEnv() {
		return JsonUtils.get().fromJson(FileManager.ENV_FILE, Env.class, new Env("default"));
	}

	private void setEnv(Env env) {
		this.env = env;
		JsonUtils.get().toJson(env, FileManager.ENV_FILE);
	}

	public void resetEnv(Env env) {
		setEnv(env);
		reset();
	}

	public void reloadEnv(Env env) {
		setEnv(env);
		reload(Mode.SILENT);
	}

	public Settings loadSettings() {
		return JsonUtils.get().fromJson(FileManager.SETTINGS_FILE, Settings.class, new Settings());
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
		JsonUtils.get().toJson(settings, FileManager.SETTINGS_FILE);
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
		snippetsById.clear();
		snippetsByName.clear();
		feedback.setMode(Mode.SILENT);
		buildJShell();
		setListener();
		if (settings.isLoadDefault()) {
			loadDefault();
		}

		if (settings.isLoadPrinting()) {
			loadPrinting();
		}

		if (settings.isLoadScripts()) {
			loadStartupScripts();
		}

		startSnippetMaxIndex = idGenerator.getMaxId();
		feedback.setMode(Mode.NORMAL);
	}

	public void reload() {
		reload(Mode.NORMAL);
	}

	private void reload(Mode mode) {

		feedback.setMode(mode);
		List<Entry<Snippet, Status>> snippets = jshell.snippets()
				.filter(s -> jshell.status(s) == Status.VALID || jshell.status(s) == Status.DROPPED)
				.map(s -> new SimpleEntry<>(s, jshell.status(s))).collect(Collectors.toList());
		reset();

		snippets.forEach(s -> {
			var newSnippets = snippetProcessor.getSnippetEvents(s.getKey().source()).stream().map(SnippetEvent::snippet)
					.collect(Collectors.toList());
			if (s.getValue() == Status.DROPPED) {
				commandProcessor.drop(newSnippets);
			}
		});

		feedback.setMode(Mode.NORMAL);
	}

	private void buildJShell() {

		close();
		try {
			String[] options = env.getOptions();
			jshell = JShell.builder()
					.executionEngine(new LocalExecutionControlProvider(), null)
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

	public void loadDefault() {
		try {
			JShellUtils.loadSnippets(jshell, getClass().getResourceAsStream("start-default.txt"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void loadPrinting() {

		try {
			JShellUtils.loadSnippets(jshell, getClass().getResourceAsStream("start-printing.txt"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadStartupScripts() {

		for (String file : settings.getStartupScripts()) {
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

	public void processBatch(String input) {
		feedback.setCached(true);
		process(input);
		feedback.flush();
	}

	private void process(String input) {

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
