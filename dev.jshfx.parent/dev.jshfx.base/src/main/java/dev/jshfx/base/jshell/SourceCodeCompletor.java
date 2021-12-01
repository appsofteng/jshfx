package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
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
    public void getCompletionItems(Predicate<CompletionItem> items) {

        int[] relativeAnchor = new int[1];
        StringBuffer relativeInput = new StringBuffer();
        int relativeCursor = inputArea.getCaretColumn();
        boolean processing = true;

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

                var suggestionWrappers = suggestions.stream().map(SuggestionWrapper::new).sorted().distinct().toList();

                for (SuggestionWrapper suggestion : suggestionWrappers) {

                    String docInput = getDocInput(relativeInput.toString(), suggestion.getSuggestion(),
                            relativeAnchor[0]);

                    List<Documentation> docs = session.getJshell().sourceCodeAnalysis().documentation(docInput,
                            docInput.length(), false);

                    var expressionType = session.getJshell().sourceCodeAnalysis().analyzeType(docInput,
                            docInput.length());

                    if (docs.isEmpty()) {

                        processing = items.test(new SuggestionCompletionItem(inputArea, suggestion.getSuggestion(),
                                absoluteAnchor, Signature.get("", expressionType, this::resolveType)));

                    } else {
                        var signatures = docs.stream().map(Documentation::signature).sorted(Comparator.comparing(s -> s, String.CASE_INSENSITIVE_ORDER)).distinct().toList();

                        for (var signature : signatures) {
                            var item = new SuggestionCompletionItem(inputArea, suggestion.getSuggestion(),
                                    absoluteAnchor, Signature.get(signature, expressionType, this::resolveType));

                            processing = items.test(item);

                            if (!processing) {
                                break;
                            }
                        }
                    }

                    if (!processing) {
                        break;
                    }
                }

                break;
            }

            QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis()
                    .listQualifiedNames(relativeInput.toString(), relativeCursor);

            if (!qualifiedNames.isResolvable()) {
                if (!qualifiedNames.getNames().isEmpty()) {

                    for (var name : qualifiedNames.getNames()) {
                        processing = items.test(new QualifiedNameCompletionItem(Signature.get(name, null, this::resolveType),
                                this::addImport));
                        if (!processing) {
                            break;
                        }
                    }

                    break;
                }
            }
        }

        items.test(null);
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

        session.getSnippetProcessor().process(newImport, 0);
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

    private static class SuggestionWrapper implements Comparable<SuggestionWrapper> {
        private Suggestion suggestion;

        public SuggestionWrapper(Suggestion suggestion) {
            this.suggestion = suggestion;
        }

        public Suggestion getSuggestion() {
            return suggestion;
        }

        @Override
        public boolean equals(Object obj) {

            boolean equal = false;

            if (obj instanceof SuggestionWrapper other) {
                equal = suggestion.continuation().equals(other.suggestion.continuation());
            }

            return equal;
        }

        @Override
        public int hashCode() {
            return suggestion.continuation().hashCode();
        }

        @Override
        public int compareTo(SuggestionWrapper other) {
            return suggestion.continuation().compareToIgnoreCase(other.suggestion.continuation());
        }

        @Override
        public String toString() {
            return suggestion.continuation();
        }
    }
}
