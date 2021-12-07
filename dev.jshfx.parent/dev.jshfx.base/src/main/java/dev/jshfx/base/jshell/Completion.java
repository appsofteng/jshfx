package dev.jshfx.base.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.jx.tools.GroupNames;
import dev.jshfx.jx.tools.Lexer;
import dev.jshfx.jx.tools.Token;

public class Completion {
    
    private CodeArea inputArea;
    private Lexer lexer;
    private CommandCompletor commandCompletion;
    private SourceCodeCompletor sourceCodeCompletion;

    public Completion(CodeArea inputArea, Session session, Lexer lexer) {
        this.inputArea = inputArea;
        this.lexer = lexer;
        commandCompletion = new CommandCompletor(inputArea, session, lexer);
        sourceCodeCompletion = new SourceCodeCompletor(inputArea, session, lexer);
    }

    public Completor getCompletor() {
        Completor completion = null;
        Token token = lexer.getTokenOnCaretPosition();
                
        if (inputArea.getText().isBlank() || token != null && token.getType().equals(GroupNames.JSHELLCOMMAND)) {
            completion = commandCompletion;
        } else {
            completion = sourceCodeCompletion;
        }
        
        return completion;
    }
}
