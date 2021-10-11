package dev.jshfx.base.jshell.commands;

import java.util.Iterator;
import java.util.Set;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Feedback;
import dev.jshfx.jfx.util.FXResourceBundle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/set", separator = " ")
public class SetCommand extends BaseCommand {

    @Option(names = "feedback", arity = "0..1", paramLabel = "<mode>", descriptionKey = "/set.feedback", completionCandidates = FeedbakcModes.class)
    private String feedback;
    
    @Option(names = "-retain", descriptionKey = "/set.-retain")
    private boolean retain;
    
	public SetCommand(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	@Override
	public void run() {

        if (feedback != null && feedback.isEmpty()) {
            String mode = commandProcessor.getSession().getSettings().getFeedbackMode();
            commandProcessor.getSession().getFeedback().commandResult(FXResourceBundle.getBundle().getString​("msg.currentFeedbackMode", mode)).flush();;
        } else if (feedback != null && !feedback.isEmpty()) {
            commandProcessor.getSession().getSettings().setFeedbackMode(feedback);
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
}
