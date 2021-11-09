package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.jx.tools.JavaSourceResolver.HtmlDoc;
import dev.jshfx.jx.tools.Signature;
import javafx.application.Platform;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

class SourceCodeCompletor extends Completor {

    private Pattern importPattern = Pattern.compile("((?:import .*)(?:\n(?:import .*\n|\\s*\n)*(?:import .*))?)");

    SourceCodeCompletor(CodeArea inputArea, Session session) {
        super(inputArea, session);
    }

    @Override
    public void getCompletionItems(Consumer<CompletionItem> items) {

        int[] relativeAnchor = new int[1];
        StringBuffer relativeInput = new StringBuffer();
        int relativeCursor = inputArea.getCaretColumn();

        for (int i = inputArea.getCurrentParagraph(); i >= 0; i--) {
            String text = inputArea.getParagraph(i).getText() + "\n";
            relativeInput.insert(0, text);
            if (i < inputArea.getCurrentParagraph()) {
                relativeCursor += text.length();
            }

            List<Suggestion> suggestions = session.getJshell().sourceCodeAnalysis()
                    .completionSuggestions(relativeInput.toString(), relativeCursor, relativeAnchor);

            if (!suggestions.isEmpty()) {

                int absoluteAnchor = inputArea.getCaretPosition() - (relativeCursor - relativeAnchor[0]);

                suggestions = suggestions.stream().sorted(Comparator.comparing(s -> s.continuation())).toList();
                Map<String, String> continuations = new HashMap<>();

                for (Suggestion suggestion : suggestions) {

                    if (continuations.containsKey(suggestion.continuation())) {
                        continue;
                    }

                    continuations.put(suggestion.continuation(), suggestion.continuation());

                    String docInput = getDocInput(relativeInput.toString(), suggestion, relativeAnchor[0]);
                    // List<Documentation> docs = Session.documentation(docInput, docInput.length(),
                    // false);
                    List<Documentation> docs = session.getJshell().sourceCodeAnalysis().documentation(docInput,
                            docInput.length(), false);

                    var expressionType = session.getJshell().sourceCodeAnalysis().analyzeType(docInput,
                            docInput.length());

                    if (docs.isEmpty()) {

                        items.accept(new SuggestionCompletionItem(inputArea, suggestion, absoluteAnchor,
                                Signature.get("", expressionType, this::resolveType)));

                    } else {
                        docs = docs.stream().sorted(Comparator.comparing(d -> d.signature())).toList();
                        docs.forEach(d -> items.accept(new SuggestionCompletionItem(inputArea, suggestion,
                                absoluteAnchor, Signature.get(d.signature(), expressionType, this::resolveType))));
                    }
                }

                break;
            }

            QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis()
                    .listQualifiedNames(relativeInput.toString(), relativeCursor);

            if (!qualifiedNames.isResolvable()) {
                if (!qualifiedNames.getNames().isEmpty()) {
                    qualifiedNames.getNames().forEach(
                            n -> items.accept(new QualifiedNameCompletionItem(Signature.get(n, null, this::resolveType),
                                    this::addImport)));

                    break;
                }
            }
        }

        items.accept(null);
    }

    private void addImport(String newImport) {

        Matcher matcher = importPattern.matcher(inputArea.getText());

        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String imports = matcher.group();
            List<String> lines = imports.lines().collect(Collectors.toCollection(() -> new ArrayList<>()));

            int index = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).compareTo(newImport) >= 0) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                lines.add(index, newImport);
            } else {
                lines.add(newImport);
            }

            var newImports = lines.stream().collect(Collectors.joining("\n"));
            Platform.runLater(() -> {
                int caret = inputArea.getCaretPosition();
                inputArea.replaceText(start, end, newImports);
                inputArea.moveTo(caret + newImport.length() + 1);
            });
        } else {
            Platform.runLater(() -> {
                int caret = inputArea.getCaretPosition();
                inputArea.insertText(0, newImport + "\n\n");
                inputArea.moveTo(caret + newImport.length() + 2);
            });
        }

        session.getSnippetProcessor().process(newImport);
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

    @Override
    public HtmlDoc loadDocumentation(CompletionItem item) {
        SourceCodeCompletionItem sourceCodeCompletionItem = (SourceCodeCompletionItem) item;
        HtmlDoc doc = null;
        if (!sourceCodeCompletionItem.getSignature().toString().isEmpty()) {

            doc = session.getJavaSourceResolver().getHtmlDoc(sourceCodeCompletionItem.getSignature());
        }

        return doc;
    }

    /*
     * Simplified reference resolution. Does not search imports for types and
     * implemented interfaces and super classes for members.
     */
    @Override
    public CompletionItem getCompletionItem(String reference, HtmlDoc data) {

        String[] parts = reference.split("#");

        String resolvedRef = reference;
        String type = parts[0];
        String member = parts.length > 1 ? parts[1] : "";

        if (type.isEmpty()) {
            type = data.signature().getTypeFullName();
        }

        int i = type.indexOf('.');

        if (i == -1) {
            QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis().listQualifiedNames(type,
                    type.length());
            if (qualifiedNames.getNames().size() == 1) {
                type = qualifiedNames.getNames().get(0);
            } else {
                type = data.packageName() + "." + type;
            }
        }

        resolvedRef = member.isEmpty() ? type : type + "." + member;

        var item = new SuggestionCompletionItem(Signature.get(resolvedRef, null, this::resolveType));

        return item;
    }

    String resolveType(String name) {
        String result = null;
        int i = name.lastIndexOf(".");
        boolean isType = true;

        if (i > -1) {
            var lastPart = name.substring(i + 1);
            QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis().listQualifiedNames(lastPart,
                    lastPart.length());

            isType = !qualifiedNames.getNames().isEmpty();
        }

        if (isType) {
            List<Documentation> docs = session.getJshell().sourceCodeAnalysis().documentation(name, name.length(),
                    false);

            if (!docs.isEmpty()) {
                result = docs.get(0).signature();
            }
        }

        return result;
    }
}
