package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.fxmisc.richtext.DocRef;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.jx.tools.JavadocUtils;
import dev.jshfx.jx.tools.Token;
import javafx.application.Platform;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import picocli.AutoComplete;
import picocli.CommandLine;

public class Completion {

    private Session session;

    public Completion(Session session) {
        this.session = session;
    }

    public Collection<CompletionItem> getCompletionItems(CodeArea inputArea) {

        String currentParagraph = inputArea.getParagraph(inputArea.getCurrentParagraph()).getText();
        
        return inputArea.getText().isBlank() || CommandProcessor.isCommand(currentParagraph)
                ? getCommandCompletionItems(inputArea)
                : getCodeCompletionItems(inputArea);
    }

    private Collection<CompletionItem> getCommandCompletionItems(CodeArea inputArea) {
        String input = inputArea.getParagraph(inputArea.getCurrentParagraph()).getText();
        int caretPosition = inputArea.getCaretColumn();
        List<String> arguments = session.getCommandProcessor().getLexer().tokenize(input, caretPosition).stream()
                .map(Token::getValue).collect(Collectors.toList());
        Token tokenOnCaret = session.getCommandProcessor().getLexer().getTokenOnCaretPosition();

        int argIndex = arguments.size() - 1;

        int positionInArg = 0;

        if (tokenOnCaret != null) {
            argIndex = tokenOnCaret.getIndex();
            positionInArg = caretPosition - tokenOnCaret.getStart();
        } else if (argIndex >= 0) {
            arguments.add("");
            argIndex++;
        }

        List<CharSequence> candidates = new ArrayList<>();
        String[] args = arguments.toArray(new String[0]);

        int anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                argIndex, positionInArg, caretPosition, candidates);

        if (candidates.size() == 1 && candidates.get(0).length() == 0 && arguments.size() > 0) {
            Platform.runLater(() -> inputArea.insertText(inputArea.getCaretPosition(), " "));

            caretPosition++;
            arguments.add("");
            argIndex++;
            positionInArg = 0;
            args = arguments.toArray(new String[0]);
            anchor = AutoComplete.complete(session.getCommandProcessor().getCommandLine().getCommandSpec(), args,
                    argIndex, positionInArg, caretPosition, candidates);
        }

        String arg = args[argIndex];

        List<CompletionItem> items = new ArrayList<>();
        int absoluteAnchor = inputArea.getCaretPosition() - (inputArea.getCaretColumn() - anchor);

        for (CharSequence candidate : candidates) {

            if (candidate.length() == 0) {
                continue;
            }

            String name = arg.substring(0, positionInArg) + candidate;
            String docCode = args.length <= 1 ? name : (args[0] + "." + name);
            
            items.add(new CommandCompletionItem(inputArea, absoluteAnchor, candidate.toString(), name, docCode,
                    this::getCommandHelp));
        }

        return items;
    }

    private String getCommandHelp(DocRef docRef) {
        String help = "";
        CommandLine subcommand = session.getCommandProcessor().getCommandLine().getSubcommands()
                .get(docRef.getDocCode());

        if (subcommand != null) {
            help = "<pre>" + subcommand.getUsageMessage() + "</pre>";
        } else {
            help = FXResourceBundle.getBundle().getStringOrDefault(docRef.getDocCode(),
                    FXResourceBundle.getBundle().getStringOrDefault(docRef.getSignature(), ""));
        }

        return help;
    }

    private Collection<CompletionItem> getCodeCompletionItems(CodeArea inputArea) {

        List<CompletionItem> items = new ArrayList<>();

        int[] relativeAnchor = new int[1];
        StringBuffer relativeInput = new StringBuffer();
        int relativeCursor = inputArea.getCaretColumn();

        for (int i = inputArea.getCurrentParagraph(); i >= 0; i--) {
            String text = inputArea.getParagraph(i).getText();
            relativeInput.insert(0, text);
            if (i < inputArea.getCurrentParagraph()) {
                relativeCursor += text.length();
            }

            List<Suggestion> suggestions = session.getJshell().sourceCodeAnalysis()
                    .completionSuggestions(relativeInput.toString(), relativeCursor, relativeAnchor);

            if (!suggestions.isEmpty()) {

                int absoluteAnchor = inputArea.getCaretPosition() - (relativeCursor - relativeAnchor[0]);

                for (Suggestion suggestion : suggestions) {
                    String docInput = getDocInput(relativeInput.toString(), suggestion, relativeAnchor[0]);
                    List<Documentation> docs = Session.documentation(docInput, docInput.length(), false);

                    if (docs.isEmpty()) {

                        items.add(new SuggestionCompletionItem(inputArea, suggestion, absoluteAnchor,
                                new DocRef(docInput)));

                    } else {
                        docs.forEach(d -> items.add(new SuggestionCompletionItem(inputArea, suggestion, absoluteAnchor,
                                new DocRef(docInput, d.signature(), this::loadDocumentation))));
                    }
                }

                break;
            }

            QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis()
                    .listQualifiedNames(relativeInput.toString(), relativeCursor);

            if (!qualifiedNames.isResolvable()) {
                if (!qualifiedNames.getNames().isEmpty()) {
                    qualifiedNames.getNames().forEach(
                            n -> items.add(new QualifiedNameCompletionItem(it -> session.getConsoleView().submit(it), n,
                                    this::loadDocumentation)));

                    break;
                }
            }
        }

        Collections.sort(items);

        return items;
    }

    private String getDocInput(String input, Suggestion suggestion, int anchor) {
        int i = suggestion.continuation().lastIndexOf("(");
        String docInput = null;
        if (i > 0) {
            docInput = input.substring(0, anchor) + suggestion.continuation().substring(0, i + 1);
        } else {
            docInput = input.substring(0, anchor) + suggestion.continuation();
        }

        return docInput;
    }

    public String loadDocumentation(DocRef docRef) {
        Map<String, String> docBlockNames = FXResourceBundle.getBundle().getStrings(JavadocUtils.getBlockTagNames());
        List<Documentation> docs = Session.documentation(docRef.getDocCode(), docRef.getDocCode().length(), true);
        String documentation = JShellUtils.getDocumentation(docs, docRef, docBlockNames);

        return documentation;
    }
}
