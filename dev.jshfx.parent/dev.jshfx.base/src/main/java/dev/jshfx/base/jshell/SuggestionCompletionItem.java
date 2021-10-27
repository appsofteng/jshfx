package dev.jshfx.base.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.jx.tools.Signature;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends SourceCodeCompletionItem {

    private CodeArea codeArea;
    private Suggestion suggestion;
    private int anchor;
    private String label = "";

    public SuggestionCompletionItem(Signature signature) {
        super(signature);
    }
    
    public SuggestionCompletionItem(CodeArea codeArea, Suggestion suggestion, int anchor, Signature signature) {
        super(signature);
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        setLabel();
    }

    private void setLabel() {
        label = isMethod() ? suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("(")) : suggestion.continuation();
        label = getSignature().toString().isEmpty() ? label : label + " - " + getSignature().toString();
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public int getAnchor() {
        return anchor;
    }

    @Override
    public void complete() {
        String completion = suggestion.continuation();

        if (isMethod() && getSignature().toString().endsWith("()") && completion.endsWith("(")) {
            completion += ")";
        }

        codeArea.replaceText(anchor, codeArea.getCaretPosition(), completion);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuggestionCompletionItem
                && ((SuggestionCompletionItem) obj).suggestion.continuation().equals(suggestion.continuation())
                && ((SuggestionCompletionItem) obj).getSignature().equals(getSignature());
    }

    @Override
    public int hashCode() {
        return (suggestion.continuation() + getSignature()).hashCode();
    }
    
    @Override
    public String toString() {

        return label;
    }

    private boolean isMethod() {
        return suggestion.continuation().contains("(");
    }
}
