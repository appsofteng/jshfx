package dev.jshfx.base.jshell;

import java.util.Collection;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.jx.tools.JavaSourceResolver.HtmlDoc;

public abstract class Completor {

    protected CodeArea inputArea;
    protected Session session;

    Completor(CodeArea inputArea, Session session) {
        this.inputArea = inputArea;
        this.session = session;
    }

    public abstract Collection<CompletionItem> getCompletionItems();
        
    public abstract HtmlDoc loadDocumentation(CompletionItem item);
    
    public abstract CompletionItem getCompletionItem(String reference, HtmlDoc data);
}
