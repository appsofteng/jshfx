package dev.jshfx.base.ui;

import java.nio.file.Path;

import dev.jshfx.fxmisc.richtext.CodeAreaWrappers;

public class EditorPane extends AreaPane {

    public EditorPane(Path p, String input) {
        super(p, input);
        CodeAreaWrappers.get(getArea(), "").style();
    }
    
    @Override
    public void setActions(Actions actions) {
        super.setActions(actions);
        
        actions.setEditorContextMenu(getArea());
    }
}
