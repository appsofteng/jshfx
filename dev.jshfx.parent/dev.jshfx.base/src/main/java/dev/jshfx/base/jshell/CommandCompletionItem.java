package dev.jshfx.base.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CompletionItem;

public class CommandCompletionItem extends CompletionItem {

    private CodeArea codeArea;
    private int anchor;
    private String continuation;
    private String commandName;
    private String name;
    private boolean replace;

    public CommandCompletionItem(CodeArea codeArea, int anchor, String continuation, String commandName, String name,
            boolean replace) {
        this.codeArea = codeArea;
        this.anchor = anchor;
        this.continuation = continuation;
        this.commandName = commandName;
        this.name = name;
        this.replace = replace;
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
        if (replace) {
            codeArea.replaceText(anchor, codeArea.getCaretPosition(), continuation);
        } else {
            codeArea.insertText(anchor, continuation);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
