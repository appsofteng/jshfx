package dev.jshfx.base.jshell;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.jshfx.jfx.concurrent.QueueTask;
import dev.jshfx.jfx.util.FXResourceBundle;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.EvalException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.VarSnippet;

public class SnippetProcessor extends Processor {

    SnippetProcessor(Session session) {
        super(session);
    }

    @Override
    public QueueTask<Void> getTask(String input) {
        QueueTask<Void> task = QueueTask.create(() -> analyseAndEvaluate(input));
        
        return task;

    }

    private void analyseAndEvaluate(String input) {

        SourceCodeAnalysis sourceAnalysis = session.getJshell().sourceCodeAnalysis();

        String[] lines = input.split("\n");
        StringBuffer sb = new StringBuffer();

        main: for (int i = 0; i < lines.length; i++) {

            sb.append(lines[i]).append("\n");
            
            while (!sb.isEmpty()) {
                CompletionInfo info = sourceAnalysis.analyzeCompletion(sb.toString());

                if (info.completeness() == Completeness.CONSIDERED_INCOMPLETE) {
                    continue main;
                } else if (info.completeness() == Completeness.DEFINITELY_INCOMPLETE) {
                    if (i == lines.length - 1) {
                        session.getFeedback()
                                .snippetError(FXResourceBundle.getBundle().getStringâ€‹("definitelyIncomplete") + "  "
                                        + sb.toString().strip() + "\n");
                    }
                    continue main;
                } else if (info.completeness() == Completeness.EMPTY) {
                    sb.delete(0, sb.length());
                    continue main;
                } else if (info.completeness() == Completeness.UNKNOWN) {
                    session.getFeedback().snippetError(
                            FXResourceBundle.getBundle().getStringâ€‹("unknown") + "  " + sb.toString().strip() + "\n");
                    sb.delete(0, sb.length());
                    continue main;
                } else if (info.completeness() == Completeness.COMPLETE
                        || info.completeness() == Completeness.COMPLETE_WITH_SEMI) {
                    if (i + 1 < lines.length && lines[i + 1].trim().startsWith(".")) {
                        sb.delete(sb.length() - 1, sb.length());
                        continue main;
                    }
                }

                String source = info.source();
                sb.delete(0, sb.length()).append(info.remaining());
                List<SnippetEvent> snippetEvents = session.getJshell().eval(source);
                snippetEvents.forEach(e -> setFeedback(e, false));
            }
        }

        session.getFeedback().flush();
    }

    public List<SnippetEvent> process(Snippet snippet, boolean quiet) {

        if (!quiet) {
            session.getFeedback().commandResult(snippet.source().strip() + "\n");
        }
        List<SnippetEvent> snippetEvents = session.getJshell().eval(snippet.source());
        snippetEvents.forEach(e -> setFeedback(e, quiet));

        session.getFeedback().flush();

        return snippetEvents;
    }

    public List<SnippetEvent> process(List<Snippet> snippets) {

        List<SnippetEvent> allSnippetEvents = new ArrayList<>();

        for (Snippet snippet : snippets) {
            session.getFeedback().commandResult(snippet.source().strip() + "\n");
            List<SnippetEvent> snippetEvents = session.getJshell().eval(snippet.source());
            allSnippetEvents.addAll(snippetEvents);
            snippetEvents.forEach(e -> setFeedback(e, false));
        }

        session.getFeedback().flush();

        return allSnippetEvents;
    }
    
    public void process(InputStream in) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            reader.lines().forEach(s -> session.getJshell().eval(s));
        }
    }

    private void setFeedback(SnippetEvent event, boolean quiet) {

        String message = "";

        if (event.exception() != null) {
            message = getExceptionMessage(event);
            message = message.strip() + "\n";
            session.getFeedback().snippetError(message);
        } else if (event.status() == Status.REJECTED) {
            message = getRejectedMessage(event);
            message = message.strip() + "\n";
            session.getFeedback().snippetError(message);
        } else if (!quiet) {
            message = getVerboseSuccessMessage(event);
            message = message.strip() + "\n";
            session.getFeedback().snippetVerbose(message);
            Snippet snippet = event.snippet();

            if (snippet.kind() == Kind.EXPRESSION || snippet.subKind() == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
                session.getFeedback().snippetExpression(message);
            } else if (snippet.kind() == Kind.TYPE_DECL || snippet.kind() == Kind.METHOD || snippet.kind() == Kind.VAR
                    || snippet.kind() == Kind.IMPORT) {
                session.getFeedback().snippetDeclaration(message);
            }
        }
    }

    private String getExceptionMessage(SnippetEvent event) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        event.exception().printStackTrace(new PrintStream(out));
        String msg = out.toString();

        if (event.exception() instanceof EvalException) {
            EvalException e = (EvalException) event.exception();
            msg = msg.replace(event.exception().getClass().getName(), e.getExceptionClassName());
        }

        msg = FXResourceBundle.getBundle().getStringâ€‹("exception") + " " + msg.replace("\r", "");

        return msg;
    }

    private String getRejectedMessage(SnippetEvent event) {
        StringBuilder sb = new StringBuilder();

        session.getJshell().diagnostics(event.snippet()).forEach(d -> {
            if (d.isError()) {
                sb.append(FXResourceBundle.getBundle().getStringâ€‹("error") + ":\n");
            }
            sb.append(d.getMessage(null)).append("\n");

            String errorLine = SnippetUtils.getErrorLine(event.snippet().source(), (int) d.getStartPosition(),
                    (int) d.getEndPosition());

            sb.append(errorLine).append("\n");
        });

        return sb.toString();
    }

    private String getVerboseSuccessMessage(SnippetEvent event) {

        if (event.previousStatus() == event.status() || event.previousStatus() == Status.RECOVERABLE_DEFINED
                || event.previousStatus() == Status.RECOVERABLE_NOT_DEFINED) {
            return "";
        }

        String msg = "";

        if (event.snippet().kind() == Kind.EXPRESSION) {
            msg = "";
        } else if (event.previousStatus() == Status.NONEXISTENT) {
            msg = FXResourceBundle.getBundle().getStringâ€‹("created");
        } else if (event.status() == Status.OVERWRITTEN) {
            if (event.causeSnippet().subKind() == event.snippet().subKind()) {
                msg = FXResourceBundle.getBundle().getStringâ€‹("modified");
            } else {
                msg = FXResourceBundle.getBundle().getStringâ€‹("replaced");
            }
        }

        String dependency = "";
        if (event.status() == Status.RECOVERABLE_DEFINED || event.status() == Status.RECOVERABLE_NOT_DEFINED) {
            Snippet snippet = event.snippet();

            if (snippet != null && snippet instanceof DeclarationSnippet) {
                String dependencies = session.getJshell().unresolvedDependencies((DeclarationSnippet) snippet)
                        .collect(Collectors.joining(", "));
                dependency = ", " + FXResourceBundle.getBundle().getStringâ€‹("undeclared", dependencies);
            }
        }

        String value = event.value();
        Snippet snippet = event.snippet();

        if (event.causeSnippet() != null) {
            snippet = event.causeSnippet();
            if (snippet instanceof VarSnippet && session.getJshell().status(snippet) == Status.VALID) {
                value = session.getJshell().varValue((VarSnippet) event.causeSnippet());
            }
        }

        msg += SnippetUtils.toString(snippet, value, session.getJshell()).stripTrailing() + dependency + "\n";

        return msg;
    }
}
