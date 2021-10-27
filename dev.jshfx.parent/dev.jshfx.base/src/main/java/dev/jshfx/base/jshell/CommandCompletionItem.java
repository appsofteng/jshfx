package dev.jshfx.base.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;

public class CommandCompletionItem extends CompletionItem {

    private CodeArea codeArea;
    private int anchor;
    private String continuation;
    private String commandName;
    private String name;

    public CommandCompletionItem(CodeArea codeArea, int anchor, String continuation, String commandName, String name) {
        this.codeArea = codeArea;
        this.anchor = anchor;
        this.continuation = continuation;
        this.commandName = commandName;
        this.name = name;
    }
    
    public String getCommandName() {
        return commandName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDocKey() {
        return commandName + "." + name;
    }

    @Override
    public void complete() {
        codeArea.insertText(anchor, continuation);
    }

    @Override
    public String toString() {
        return name;
    }
}
