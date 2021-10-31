package dev.jshfx.base.ui;

import java.nio.file.Path;
import java.util.List;

import dev.jshfx.base.sys.TaskManager;
import dev.jshfx.jfx.concurrent.CTask;
import dev.jshfx.jfx.util.FXResourceBundle;

public class ActionController {

    protected RootPane rootPane;
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
        actions.unbind();
        actions.savedProperty().bind(contentPane.modifiedProperty().not());
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

        String name = FXResourceBundle.getBundle().getString​("new");
        int i = 0;
        while (rootPane.exists(name + i)) {
            i++;
        }

        rootPane.addSelect(contentPaneFactory.newShellPane(name + i));
    }

    public void openFile() {
        List<Path> files = FileDialogUtils.getJavaFiles(rootPane.getScene().getWindow());
        List<Path> newFiles = rootPane.getNew(files);
        if (!files.isEmpty()) {
            TaskManager.get().execute(CTask.create(() -> contentPaneFactory.create(newFiles))
                    .onSucceeded(contentPanes -> rootPane.add(contentPanes)));
        }
    }

    public void saveFile() {

    }

    public void saveAsFile() {

    }

    public void save(ContentPane contentPane) {

    }
}
