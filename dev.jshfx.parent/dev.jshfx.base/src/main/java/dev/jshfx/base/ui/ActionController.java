package dev.jshfx.base.ui;

import java.nio.file.Path;
import java.util.List;

public class ActionController {

    private RootPane rootPane;
    private ContentPaneFactory contentPaneFactory;
    
    protected Actions actions;

    public ActionController(RootPane rootPane, Actions actions) {
        this.rootPane = rootPane;
        this.actions = actions;
        contentPaneFactory = new ContentPaneFactory();
    }

    public void init(ContentPane contentPane) {
        
    }
    
    public void bind(ContentPane contentPane) {
        actions.allSelectedProperty().unbind();
        actions.clearProperty().unbind();
        actions.selectionEmptyProperty().unbind();
        actions.redoEmptyProperty().unbind();
        actions.undoEmptyProperty().unbind();
        actions.historyStartReachedProperty().unbind();
        actions.historyEndReachedProperty().unbind();
    }
    
    public void copy() {

    }

    public void cut() {

    }

    public void paste() {

    }

    public void selectAll() {

    }

    public void clear() {

    }

    public void undo() {

    }

    public void redo() {

    }

    public void submit() {

    }

    public void submitLine() {

    }

    public void eval() {

    }

    public void evalLine() {

    }

    public void historyUp() {

    }

    public void historyDown() {

    }

    public void insertDirPath() {

    }

    public void insertFilePaths() {

    }

    public void insertSaveFilePath() {

    }

    public void showCodeCompletion() {

    }

    public void newShell() {
        rootPane.newTab(contentPaneFactory.newShellPane());
    }

    public void openFile() {
       List<Path> files = FileDialogUtils.getJavaFiles(rootPane.getScene().getWindow());
    }

    public void saveFile() {

    }

    public void saveAsFile() {

    }

    public void saveAll() {

    }
}
