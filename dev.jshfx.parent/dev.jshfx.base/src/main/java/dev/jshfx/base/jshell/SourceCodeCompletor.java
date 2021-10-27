package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.jx.tools.Signature;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

class SourceCodeCompletor extends Completor {

    SourceCodeCompletor(CodeArea inputArea, Session session) {
        super(inputArea, session);
    }

    @Override
    public Collection<CompletionItem> getCompletionItems() {

        List<CompletionItem> items = new ArrayList<>();

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

                for (Suggestion suggestion : suggestions) {
                    String docInput = getDocInput(relativeInput.toString(), suggestion, relativeAnchor[0]);
                    List<Documentation> docs = Session.documentation(docInput, docInput.length(), false);

                    var expressionType = session.getJshell().sourceCodeAnalysis().analyzeType(docInput,
                            docInput.length());

                    if (docs.isEmpty()) {

                        items.add(new SuggestionCompletionItem(inputArea, suggestion, absoluteAnchor,
                                Signature.get("", expressionType, this::resolveType)));

                    } else {
                        docs.forEach(d -> items.add(new SuggestionCompletionItem(inputArea, suggestion, absoluteAnchor,
                                Signature.get(d.signature(), expressionType, this::resolveType))));
                    }
                }

                break;
            }

            QualifiedNames qualifiedNames = session.getJshell().sourceCodeAnalysis()
                    .listQualifiedNames(relativeInput.toString(), relativeCursor);

            if (!qualifiedNames.isResolvable()) {
                if (!qualifiedNames.getNames().isEmpty()) {
                    qualifiedNames.getNames().forEach(
                            n -> items.add(new QualifiedNameCompletionItem(Signature.get(n, null, this::resolveType),
                                    it -> session.getConsoleView().submit(it))));

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

    public String loadDocumentation(CompletionItem item) {
        SourceCodeCompletionItem sourceCodeCompletionItem = (SourceCodeCompletionItem) item;
        String doc = "";
        if (!sourceCodeCompletionItem.getSignature().toString().isEmpty()) {

            doc = session.getJavaSourceResolver().getHtmlDoc(sourceCodeCompletionItem.getSignature());
        }

        return doc;
    }

    @Override
    public CompletionItem getCompletionItem(String url) {
        var item = new SuggestionCompletionItem(Signature.get(url, null, this::resolveType));
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
            List<Documentation> docs = Session.documentation(name, name.length(), false);

            if (!docs.isEmpty()) {
                result = docs.get(0).signature();
            }
        }

        return result;
    }
}
