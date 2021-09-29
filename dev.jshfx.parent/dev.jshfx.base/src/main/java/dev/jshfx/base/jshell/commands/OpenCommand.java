package dev.jshfx.base.jshell.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/open")
public class OpenCommand extends BaseCommand {

	@Parameters(arity = "0..1", paramLabel = "file", descriptionKey = "/open.file")
	private String file;

	public OpenCommand(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	@Override
	public void run() {

		if ("default".equalsIgnoreCase(file)) {
			commandProcessor.getSession().loadDefault();
		} else if ("printing".equalsIgnoreCase(file)) {
			commandProcessor.getSession().loadPrinting();
		} else if (file != null) {
			var path = Path.of(file);
			if (Files.exists(path)) {

				try {
					String spippets = Files.readString(path);
					commandProcessor.getSession().processBatch(spippets);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				commandProcessor.getSession().getFeedback().normal(FXResourceBundle.getBundle().getString​("msg.fileNotFound", file));
			}
		} else {
			super.run();
		}
	}
}
