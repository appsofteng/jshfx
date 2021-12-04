package dev.jshfx.base.jshell;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.jx.tools.Signature;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends SourceCodeCompletionItem {

    private CodeArea codeArea;
    private Suggestion suggestion;
    private int anchor;
    private String label = "";
    private Consumer<String> input;

    public SuggestionCompletionItem(Signature signature) {
        super(signature);
    }

    public SuggestionCompletionItem(CodeArea codeArea, Suggestion suggestion, int anchor, Signature signature,
            Consumer<String> input) {
        super(signature);
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        this.input = input;
        setLabel();
    }

    private void setLabel() {
        label = getSignature().getKind() == Signature.Kind.METHOD
                ? suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("("))
                : suggestion.continuation();
        label = getSignature().toString().isEmpty() ? label : label + " - " + getSignature().toString();
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public int getAnchor() {
        return anchor;
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(getSignature().getModifiers());
    }
    
    @Override
    public void complete() {
        complete(anchor);
    }

    @Override
    public void completeStatic() {
        var staticAnchor = anchor;
        
        while (--staticAnchor >= 0 && !Character.isWhitespace(codeArea.getText().charAt(staticAnchor))) {}
        staticAnchor++;
        
        complete(staticAnchor);
        var stat = getSignature().getKind() == Signature.Kind.TYPE ? "" : " static";
        input.accept(String.format("import%s %s", stat, getSignature().getCanonicalName()));
    }

    private void complete(int anchor) {
        String completion = suggestion.continuation();

        if (getSignature().getKind() == Signature.Kind.METHOD && getSignature().toString().endsWith("()")
                && completion.endsWith("(")) {
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
    
    static void mymethod(int a) {}
     void mymethod(int a, int b) {}
}
