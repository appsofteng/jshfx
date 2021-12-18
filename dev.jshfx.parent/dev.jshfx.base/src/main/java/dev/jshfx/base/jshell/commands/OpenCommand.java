package dev.jshfx.base.jshell.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Settings;
import dev.jshfx.j.nio.file.PathUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/open")
public class OpenCommand extends BaseCommand {

	@Parameters(arity = "0..1", descriptionKey = "/open.file", completionCandidates = FiletOptions.class)
	private String file;

	public OpenCommand(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	@Override
	public void run() {

		if (Settings.PREDEFINED_STARTUP_FILES.keySet().contains(file)) {
			commandProcessor.getSession().loadPredefinedStartupFile(file);
		} else if (file != null) {
			var path = Path.of(file);
			path = commandProcessor.getSession().getCurDir().resolve(path);
			if (Files.exists(path)) {
				try {
					String spippets = Files.readString(path);
					commandProcessor.getSession().process(spippets);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				commandProcessor.getSession().getFeedback().commandFailure(FXResourceBundle.getBundle().getString​("msg.fileNotFound", file)).flush();
			}
		} else {
			super.run();
		}
	}
	
    public static class FiletOptions implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return Settings.PREDEFINED_STARTUP_FILES.keySet().iterator();
        }
    }
}
