package dev.jshfx.base.jshell;

import java.util.function.Consumer;

import dev.jshfx.jx.tools.Signature;

public class QualifiedNameCompletionItem extends SourceCodeCompletionItem {

    private final Consumer<String> input;

    public QualifiedNameCompletionItem(Signature signature, Consumer<String> input) {
       super(signature);
        this.input = input;
    }

    @Override
    public void complete() {
        input.accept(String.format("import %s", getSignature().toString()));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QualifiedNameCompletionItem
                && ((QualifiedNameCompletionItem) obj).getSignature().equals(getSignature());
    }

    @Override
    public int hashCode() {
        return getSignature().hashCode();
    }
    
    @Override
    public String toString() {
        return getSignature().getSimpleName() + " - " + getSignature().getCanonicalName();
    }
}
