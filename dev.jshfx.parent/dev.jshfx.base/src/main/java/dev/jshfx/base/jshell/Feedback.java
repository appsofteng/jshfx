package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import dev.jshfx.jfx.scene.control.ConsoleModel;

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

    private static final Map<String, List<String>> MAPPING = Map.of(VERBOSE,
            List.of(UPDATE, COMMAND_SUCCESS, COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_VERBOSE,
                    SNIPPET_ERROR),
            NORMAL, List.of(COMMAND_SUCCESS, COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_EXPRESSION, SNIPPET_DECLARATION, SNIPPET_ERROR),
            CONCISE, List.of(COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_EXPRESSION, SNIPPET_ERROR), SILENT,
            List.of(COMMAND_RESULT, COMMAND_FAILURE, SNIPPET_ERROR));

    private Map<String, List<TextStyleSpans>> feedback = new HashMap<>();

    private ConsoleModel consoleModel;
    private Settings settings;

    public Feedback(ConsoleModel consoleModel, Settings settings) {
        this.consoleModel = consoleModel;
        this.settings = settings;
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

        add(COMMAND_FAILURE, message, ConsoleModel.COMMENT_STYLE);

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

        feedback.computeIfAbsent(type, k -> new ArrayList<>()).add(new TextStyleSpans(message));

    }
    
    private void add(String type, String message, String style) {

        feedback.computeIfAbsent(type, k -> new ArrayList<>()).add(new TextStyleSpans(message, style));
    }
    
    public void flush() {

        getFeedback().forEach(s -> consoleModel.addNewLineOutput(s));
        feedback.clear();
    }

    private List<TextStyleSpans> getFeedback() {
        return MAPPING.get(settings.getFeedbackMode()).stream()
                .flatMap(m -> feedback.getOrDefault(m, List.of()).stream()).collect(Collectors.toList());
    }

//    public void normal(String message) {
//        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
//            normal(new TextStyleSpans(message));
//        }
//    }
//
//    public void normaln(String message) {
//        normal(message + "\n");
//    }
//
//    public void normal(String message, String style) {
//        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
//            normal(new TextStyleSpans(message, style));
//        }
//    }
//
//    public void normaln(String message, String style) {
//        normal(message + "\n", style);
//    }
//
//    public void normal(TextStyleSpans span) {
//        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
//            if (cached) {
//                cache.add(span);
//            } else {
//                consoleModel.addNewLineOutput(span);
//            }
//        }
//    }
}
