package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import picocli.AutoComplete;
import picocli.CommandLine;

public class Completion {

    private Session session;

    public Completion(Session session) {
        this.session = session;
    }

    public Collection<CompletionItem> getCompletionItems(CodeArea inputArea) {

        return inputArea.getText().isBlank() || CommandProcessor.isCommand(inputArea.getText())
                ? getCommandCompletionItems(inputArea)
                : getCodeCompletionItems(inputArea);
    }

    private Collection<CompletionItem> getCommandCompletionItems(CodeArea inputArea) {
        String input = inputArea.getText();
        int caretPosition = inputArea.getCaretPosition();
        List<String> arguments = session.getCommandProcessor().getLexer().tokenize(input, caretPosition).stream()
                .map(Token::getValue).collect(Collectors.toList());
        Token tokenOnCaret = session.getCommandProcessor().getLexer().getTokenOnCaretPosition();
        
        int argIndex = arguments.size() - 1;;
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
            Platform.runLater(() ->  inputArea.insertText(inputArea.getCaretPosition(), " "));
           
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

        for (CharSequence candidate : candidates) {

            if (candidate.length() == 0) {
                continue;
            }

            String name = arg.substring(0, positionInArg) + candidate;
            String docCode = args.length <= 1 ? name : (args[0] + "." + name);

            items.add(new CommandCompletionItem(inputArea, anchor, candidate.toString(), name, docCode,
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

        String code = inputArea.getText();
        int cursor = inputArea.getCaretPosition();

        int[] anchor = new int[1];

        Set<SuggestionCompletionItem> suggestionItems = session.getJshell().sourceCodeAnalysis()
                .completionSuggestions(code, cursor, anchor).stream()
                .map(s -> new SuggestionCompletionItem(inputArea, code, s, anchor)).collect(Collectors.toSet());

        for (SuggestionCompletionItem item : suggestionItems) {

            List<Documentation> docs = Session.documentation(item.getDocRef().getDocCode(),
                    item.getDocRef().getDocCode().length(), false);

            if (docs.isEmpty()) {
                items.add(item);
            } else {
                items.addAll(docs.stream()
                        .map(d -> new SuggestionCompletionItem(inputArea, item.getSuggestion(), item.getAnchor(),
                                item.getDocRef().getDocCode(), d.signature(), this::loadDocumentation))
                        .collect(Collectors.toSet()));
            }
        }

        Collections.sort(items);

        QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis().listQualifiedNames(code, cursor);

        if (!qualifiedNames.isResolvable()) {
            Set<CompletionItem> names = qualifiedNames.getNames().stream()
                    .map(n -> new QualifiedNameCompletionItem(i -> session.getConsoleView().enter(i), n,
                            this::loadDocumentation))
                    .sorted().collect(Collectors.toSet());

            items.addAll(names);
        }

        return items;
    }

    public String loadDocumentation(DocRef docRef) {
        Map<String, String> docBlockNames = FXResourceBundle.getBundle().getStrings(JavadocUtils.getBlockTagNames());
        List<Documentation> docs = Session.documentation(docRef.getDocCode(), docRef.getDocCode().length(), true);
        String documentation = JShellUtils.getDocumentation(docs, docRef, docBlockNames);

        return documentation;
    }
}
