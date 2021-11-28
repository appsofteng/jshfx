package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jshfx.base.ui.ConsoleModel;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;

public class Feedback {

    public static final String VERBOSE = "verbose";
    public static final String NORMAL = "normal";
    public static final String CONCISE = "concise";
    public static final String SILENT = "silent";

    private static final String COMMAND_RESULT = "Command Result";
    private static final String COMMAND_SUCCESS = "Command Success";
    private static final String COMMAND_FAILURE = "Command Failure";
    private static final String SNIPPET_VERBOSE = "Snippet Verbose";
    private static final String SNIPPET_DECLARATION = "Snippet Declaration";
    private static final String SNIPPET_EXPRESSION = "Snippet Expression";    
    private static final String SNIPPET_ERROR = "Snippet Error";    
    private static final String UPDATE = "Update";

    public static final Set<String> MODES = Set.of(CONCISE, NORMAL, SILENT, VERBOSE);
    
    private static final Map<String, List<String>> MAPPING = Map.of(VERBOSE,
            List.of(UPDATE, COMMAND_SUCCESS, COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_VERBOSE,
                    SNIPPET_ERROR),
            NORMAL, List.of(COMMAND_SUCCESS, COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_EXPRESSION, SNIPPET_DECLARATION, SNIPPET_ERROR),
            CONCISE, List.of(COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_EXPRESSION, SNIPPET_ERROR), SILENT,
            List.of(COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_ERROR));

    private List<FeedbackItem> feedback = new ArrayList<>();

    private ConsoleModel consoleModel;
    private Settings settings;

    public Feedback(ConsoleModel consoleModel, Settings settings) {
        this.consoleModel = consoleModel;
        this.settings = settings;
    }
    
    public boolean isValid(String mode) {
        return MODES.contains(mode);
    }
    
    
    public Feedback commandResult(String message) {

        add(COMMAND_RESULT, message);

        return this;
    }
    
    public Feedback commandSuccess(String message) {

        add(COMMAND_SUCCESS, message, ConsoleModel.COMMENT_STYLE);

        return this;
    }
    
    public Feedback commandFailure(String message) {

        add(COMMAND_FAILURE, message, ConsoleModel.ERROR_STYLE);

        return this;
    }
    
    public Feedback snippetVerbose(String message) {
        add(SNIPPET_VERBOSE, message, ConsoleModel.COMMENT_STYLE);

        return this;
    }
    
    public Feedback snippetExpression(String message) {
        add(SNIPPET_EXPRESSION, message, ConsoleModel.COMMENT_STYLE);

        return this;
    }
    
    public Feedback snippetDeclaration(String message) {
        add(SNIPPET_DECLARATION, message, ConsoleModel.COMMENT_STYLE);

        return this;
    }
    
    public Feedback snippetError(String message) {
        add(SNIPPET_ERROR, message, ConsoleModel.ERROR_STYLE);

        return this;
    }

    private void add(String type, String message) {

        feedback.add(new FeedbackItem(type, new TextStyleSpans(message)));

    }
    
    private void add(String type, String message, String style) {

        feedback.add(new FeedbackItem(type, new TextStyleSpans(message, style)));
    }
    
    public void flush() {

        getFeedback().forEach(s -> consoleModel.addNewLineOutput(s));
        feedback.clear();
    }

    private List<TextStyleSpans> getFeedback() {
        return feedback.stream().filter(i-> MAPPING.get(settings.getFeedbackMode()).contains(i.type()))
                .map(FeedbackItem::message)
                .collect(Collectors.toList());
    }
    
    record FeedbackItem(String type, TextStyleSpans message) {}
}
