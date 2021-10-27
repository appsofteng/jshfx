package dev.jshfx.base.jshell;

import org.fxmisc.richtext.CodeArea;

public class Completion {
    
    private CodeArea inputArea;
    private CommandCompletor commandCompletion;
    private SourceCodeCompletor sourceCodeCompletion;

    public Completion(CodeArea inputArea, Session session) {
        this.inputArea = inputArea;
        commandCompletion = new CommandCompletor(inputArea, session);
        sourceCodeCompletion = new SourceCodeCompletor(inputArea, session);
    }

    public Completor getCompletor() {
        Completor completion = null;
        
        String currentParagraph = inputArea.getParagraph(inputArea.getCurrentParagraph()).getText();
        
        if (inputArea.getText().isBlank() || CommandProcessor.isCommand(currentParagraph)) {
            completion = commandCompletion;
        } else {
            completion = sourceCodeCompletion;
        }
        
        return completion;
    }
}
