package dev.jshfx.base.jshell;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.jx.tools.Signature;

public abstract class SourceCodeCompletionItem extends CompletionItem {
    
    private Signature signature;

    public SourceCodeCompletionItem(Signature signature) {
        this.signature = signature;
    }
    
    public Signature getSignature() {
        return signature;
    }
}
