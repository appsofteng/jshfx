package dev.jshfx.base.ui;

import java.nio.file.Path;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jshfx.fxmisc.richtext.CustomCodeArea;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class AreaPane extends ContentPane {

    private CodeArea area = new CustomCodeArea();
    private final ReadOnlyBooleanWrapper edited = new ReadOnlyBooleanWrapper();
    private Finder finder;

    public AreaPane(Path p, String input) {
        super(p);
        area.replaceText(input);
        area.moveTo(0);
        area.requestFollowCaret();
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> edited.set((Boolean) n));
        area.textProperty().addListener((v, o, n) -> edited.set(true));
        modified.bind(edited.getReadOnlyProperty());
        getChildren().add(new VirtualizedScrollPane<>(area));
        forgetEdit();
        finder = new FinderImpl(area);
    }
    
    @Override
    public void setActions(Actions actions) {
        super.setActions(actions);
        
        handlers.put(actions.getCopyAction(), () -> area.copy());
        handlers.put(actions.getCutAction(), () -> area.cut());
        handlers.put(actions.getPasteAction(), () -> area.paste());
        handlers.put(actions.getSelectAllAction(), () -> area.selectAll());
        handlers.put(actions.getClearAction(), () -> area.clear());
        handlers.put(actions.getUndoAction(), () -> area.undo());
        handlers.put(actions.getRedoAction(), () -> area.redo());
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        area.requestFocus();
    }
    
    public CodeArea getArea() {
        return area;
    }

    private void forgetEdit() {
        edited.set(false);
        area.getUndoManager().forgetHistory();
    }

    @Override
    public void saved(Path path) {
        super.saved(path);
        forgetEdit();
    }

    @Override
    public String getSelection() {
        return area.getSelectedText();
    }

    @Override
    public String getContent() {
        return area.getText();
    }

    @Override
    public Finder getFinder() {
        return finder;
    }

    @Override
    public void dispose() {
        area.dispose();
    }
}
