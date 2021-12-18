package dev.jshfx.base.ui;

import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jshfx.fxmisc.richtext.AreaWrapper;
import dev.jshfx.fxmisc.richtext.CustomCodeArea;
import dev.jshfx.j.nio.file.PathUtils;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class AreaPane extends ContentPane {

    private CodeArea area = new CustomCodeArea();
    private AreaWrapper<CodeArea> areaWrapper = new AreaWrapper<>(area);
    private final ReadOnlyBooleanWrapper edited = new ReadOnlyBooleanWrapper();
    private Finder finder;

    public AreaPane(Path p, String input) {
        super(p);
        wrap(area);
        area.replaceText(input);
        area.moveTo(0);
        area.requestFollowCaret();
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getUndoManager().atMarkedPositionProperty().addListener((v, o, n) -> edited.set(!n));
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
    public void bindActions(Actions actions) {
        super.bindActions(actions);

        actions.getSelectAllAction().disabledProperty().bind(areaWrapper.allSelectedProperty());
        actions.getCopyAction().disabledProperty().bind(areaWrapper.selectionEmptyProperty());
        actions.getCutAction().disabledProperty().bind(areaWrapper.selectionEmptyProperty());
        actions.getClearAction().disabledProperty().bind(areaWrapper.clearProperty());
        actions.getUndoAction().disabledProperty().bind(areaWrapper.undoEmptyProperty());
        actions.getRedoAction().disabledProperty().bind(areaWrapper.redoEmptyProperty());
    }

    protected void insertDirPath() {
        var dir = FileDialogUtils.getDirectory(getScene().getWindow());

        dir.ifPresent(d -> {
            getArea().insertText(getArea().getCaretPosition(), FilenameUtils.separatorsToUnix(d.toString()));
        });
    }

    protected void insertFilePaths() {
        insertFilePaths(null, " ");
    }

    protected void insertRelativeFilePaths() {
        insertFilePaths(getFXPath().getPath().getParent(), " ");
    }
    
    protected void insertFilePaths(String separator) {
        insertFilePaths(null, separator);
    }
    
    private void insertFilePaths(Path parent, String separator) {
        var files = FileDialogUtils.openJavaFiles(getScene().getWindow());

        String path = files.stream().map(f -> PathUtils.relativize(parent, f))
                .map(f -> FilenameUtils.separatorsToUnix(f.toString())).collect(Collectors.joining(separator));

        getArea().insertText(getArea().getCaretPosition(), path);
    }   

    protected void insertSaveFilePath() {
        var file = FileDialogUtils.saveSourceJavaFile(getScene().getWindow());

        file.ifPresent(f -> {
            getArea().insertText(getArea().getCaretPosition(), FilenameUtils.separatorsToUnix(f.toString()) + " ");
        });
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        area.requestFocus();
    }

    public CodeArea getArea() {
        return area;
    }

    protected void wrap(CodeArea area) {

    }

    private void forgetEdit() {
        edited.set(false);
        area.getUndoManager().forgetHistory();
    }

    @Override
    public void saved(Path path) {
        super.saved(path);
        edited.set(false);
        area.getUndoManager().mark();
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
        super.dispose();
        area.dispose();
    }
}
