package dev.jshfx.base.jshell;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jshfx.base.jshell.commands.Commands;
import dev.jshfx.base.jshell.commands.DropCommand;
import dev.jshfx.base.jshell.commands.EnvCommand;
import dev.jshfx.base.jshell.commands.ExitCommand;
import dev.jshfx.base.jshell.commands.HelpCommand;
import dev.jshfx.base.jshell.commands.HistoryCommand;
import dev.jshfx.base.jshell.commands.ImportCommand;
import dev.jshfx.base.jshell.commands.ListCommand;
import dev.jshfx.base.jshell.commands.MethodCommand;
import dev.jshfx.base.jshell.commands.OpenCommand;
import dev.jshfx.base.jshell.commands.ReloadCommand;
import dev.jshfx.base.jshell.commands.RerunCommand;
import dev.jshfx.base.jshell.commands.ResetCommand;
import dev.jshfx.base.jshell.commands.SaveCommand;
import dev.jshfx.base.jshell.commands.SetCommand;
import dev.jshfx.base.jshell.commands.StopCommand;
import dev.jshfx.base.jshell.commands.TypeCommand;
import dev.jshfx.base.jshell.commands.VarCommand;
import dev.jshfx.jfx.scene.control.ConsoleModel;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.jx.tools.Lexer;
import dev.jshfx.jx.tools.Token;
import jdk.jshell.Snippet;
import picocli.CommandLine;

public class CommandProcessor extends Processor {

    public static final String OPTION_SEPARATOR = " ";
	private static final List<String> PRIVILEGED_COMMANDS = List.of(ExitCommand.EXIT_COMMAND, StopCommand.STOP_COMMAND);
	static final String COMMAND_PATTERN = "^/[\\w!?\\-]*( .*)*$";
    private static final String COMMANDS_FILE = "commands";
	private CommandLine commandLine;
	private PrintWriter out;
	private DropCommand dropCommand;
	private CompletableFuture<CommandLine> future;
	private Lexer lexer;

	CommandProcessor(Session session) {
		super(session);

		lexer = Lexer.get(COMMANDS_FILE);
		future = CompletableFuture.supplyAsync(this::createCommands).thenApplyAsync(this::loadDoc);
	}
	
	public Lexer getLexer() {
        return lexer;
    }

	private CommandLine createCommands() {
		var os = session.getConsoleModel().getOut(ConsoleModel.HELP_STYLE);
		this.out = new PrintWriter(os, true);
		
		// Necessary to prevent the default strategy from resetting the out and err
		// to the default System.out and System.err.
		var strategy = new CommandLine.RunLast();
		strategy.useOut(new PrintStream(os, true));
		strategy.useErr(session.getConsoleModel().getErr());
		
		CommandLine commandLine = new CachingCommandLine(new Commands())
				.addSubcommand(new CachingCommandLine(dropCommand = new DropCommand(this)))
				.addSubcommand(new CachingCommandLine(new EnvCommand(this)))
				.addSubcommand(new CachingCommandLine(new ExitCommand(this)))
				.addSubcommand(new CachingCommandLine(new HelpCommand(this)))
				.addSubcommand(new CachingCommandLine(new HistoryCommand(this)))
				.addSubcommand(new CachingCommandLine(new ImportCommand(this)))
				.addSubcommand(new CachingCommandLine(new ListCommand(this)))
				.addSubcommand(new CachingCommandLine(new MethodCommand(this)))
				.addSubcommand(new CachingCommandLine(new OpenCommand(this)))
				.addSubcommand(new CachingCommandLine(new ReloadCommand(this)))
				.addSubcommand(new CachingCommandLine(new RerunCommand(this)))
				.addSubcommand(new CachingCommandLine(new ResetCommand(this)))
				.addSubcommand(new CachingCommandLine(new SaveCommand(this)))
				.addSubcommand(new CachingCommandLine(new SetCommand(this)))
				.addSubcommand(new CachingCommandLine(new StopCommand(this)))
				.addSubcommand(new CachingCommandLine(new TypeCommand(this)))
				.addSubcommand(new CachingCommandLine(new VarCommand(this))).setOut(out)
				.setErr(new PrintWriter(session.getConsoleModel().getErr(), true))
				.setResourceBundle(FXResourceBundle.getBundle().getResourceBundle())
				.setExecutionStrategy(strategy);

		return commandLine;
	}

	private CommandLine loadDoc(CommandLine commandLine) {
		Map<String, CommandLine> commands = new HashMap<>(commandLine.getSubcommands());
		commands.put("", commandLine);
		// load and cache in parallel
		commands.entrySet().parallelStream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getUsageMessage()));
		return commandLine;
	}

	public void drop(List<Snippet> snippets) {
		dropCommand.drop(snippets);
	}

	public List<Snippet> matches(String[] values) {
		return matches(Arrays.asList(values));
	}

	public List<Snippet> matches(List<String> values) {

		List<Snippet> snippets = new ArrayList<>();

		for (String value : values) {
			if (value.matches("\\d+")) {
				Snippet s = session.getSnippetsById().get(value);
				if (s != null) {
					snippets.add(s);
				}
			} else if (value.matches("\\d+-\\d+")) {
				String[] parts = value.split("-");
				snippets.addAll(IntStream.rangeClosed(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]))
						.mapToObj(i -> session.getSnippetsById().get(String.valueOf(i))).filter(s -> s != null)
						.collect(Collectors.toList()));
			} else {
				snippets.addAll(session.getSnippetsByName().getOrDefault(value, List.of()));
			}
		}

		return snippets;
	}

	public CommandLine getCommandLine() {

		if (commandLine == null) {
			try {
				commandLine = future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return commandLine;
	}

	public PrintWriter getOut() {
		return out;
	}

	@Override
	void process(String input) {

	    List<String> args = lexer.tokenize(input).stream().map(Token::getValue).collect(Collectors.toCollection(() -> new ArrayList<>()));
	    
		if (args.size() > 0) {

			RerunCommand.setIfMatches(args);
		}

		String[] arguments = args.toArray(new String[0]);

		if (PRIVILEGED_COMMANDS.stream().anyMatch(c -> input.startsWith(c))) {
			getSession().getTaskQueuer().add(Session.PRIVILEDGED_TASK_QUEUE, () -> getCommandLine().execute(arguments));
		} else {
			getSession().getTaskQueuer().add(() -> getCommandLine().execute(arguments));
		}
	}

	static boolean isCommand(String input) {
		return input.matches(COMMAND_PATTERN);
	}

	private static class CachingCommandLine extends CommandLine {

		private String cache;

		public CachingCommandLine(Object command) {
			super(command);
		}

		@Override
		public String getUsageMessage() {
			return usage(new StringBuilder(), getColorScheme());
		}

		@Override
		public void usage(PrintStream out, Help.ColorScheme colorScheme) {
			out.print(usage(new StringBuilder(), colorScheme));
			out.flush();
		}

		@Override
		public void usage(PrintWriter writer, Help.ColorScheme colorScheme) {
			writer.print(usage(new StringBuilder(), colorScheme));
			writer.flush();
		}

		private synchronized String usage(StringBuilder sb, Help.ColorScheme colorScheme) {

			Help help = getHelpFactory().create(getCommandSpec(), colorScheme);

			if (cache == null) {
				for (String key : getHelpSectionKeys()) {
					IHelpSectionRenderer renderer = getHelpSectionMap().get(key);
					if (renderer != null) {
						sb.append(renderer.render(help));
					}
				}
				cache = sb.toString();
			}
			return cache;
		}
	}
}
