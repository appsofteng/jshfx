package dev.jshfx.base.ui;

import java.nio.file.Path;

import dev.jshfx.fxmisc.richtext.CodeAreaWrappers;

public class EditorPane extends AreaPane {

    public EditorPane(Path p, String input) {
        super(p, input);
        CodeAreaWrappers.get(getArea(), "").style().find();
    }
    
    @Override
    public void setActions(Actions actions) {
        super.setActions(actions);
        
        actions.setEditorContextMenu(getArea());
        actions.addEditorKeyHandlers(getArea());
    }
    
    @Override
    public void bindActions(Actions actions) {
        super.bindActions(actions);
        
        actions.getEvalAction().setDisabled(true);
        actions.getEvalLineAction().setDisabled(true);
        actions.getSubmitAction().setDisabled(true);
        actions.getSubmitLineAction().setDisabled(true);
    }
}
