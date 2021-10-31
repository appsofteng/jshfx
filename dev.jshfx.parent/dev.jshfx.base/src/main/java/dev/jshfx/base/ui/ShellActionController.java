package dev.jshfx.base.ui;

import java.nio.file.Files;
import java.nio.file.Path;

import dev.jshfx.base.sys.TaskManager;
import dev.jshfx.jfx.concurrent.CTask;
import javafx.beans.binding.Bindings;

public class ShellActionController extends ActionController {

    private ShellPane shellPane;

    public ShellActionController(RootPane rootPane, Actions actions, ShellPane shellPane) {
        super(rootPane, actions);
        this.shellPane = shellPane;
    }

    @Override
    public void init(ContentPane contentPane) {
        if (contentPane instanceof ShellPane shellPane) {
            actions.setEditContextMenu(shellPane.getConsolePane().getInputArea());
            actions.setReadOnlyContextMenu(shellPane.getConsolePane().getOutputArea());
        }
    }

    @Override
    public void bind(ContentPane contentPane) {
        super.bind(contentPane);

        if (contentPane instanceof ShellPane shellPane) {
            this.shellPane = shellPane;
            var inputArea = shellPane.getConsolePane().getInputArea();
            var outputArea = shellPane.getConsolePane().getOutputArea();

            actions.allSelectedProperty().bind(Bindings.createBooleanBinding(
                    () -> shellPane.getConsolePane().getFocusedArea() == null
                            || shellPane.getConsolePane().getFocusedArea() != null
                                    && shellPane.getConsolePane().getFocusedArea().getSelectedText()
                                            .length() == shellPane.getConsolePane().getFocusedArea().getText().length(),
                    shellPane.getConsolePane().focusedAreaProperty(), inputArea.selectedTextProperty(),
                    outputArea.selectedTextProperty()));

            actions.selectionEmptyProperty().bind(Bindings.createBooleanBinding(
                    () -> shellPane.getConsolePane().getFocusedArea() == null
                            || shellPane.getConsolePane().getFocusedArea() != null
                                    && shellPane.getConsolePane().getFocusedArea().getSelection().getLength() == 0,
                    shellPane.getConsolePane().focusedAreaProperty(), inputArea.selectionProperty(),
                    outputArea.selectionProperty()));

            actions.clearProperty()
                    .bind(Bindings.createBooleanBinding(
                            () -> shellPane.getConsolePane().getFocusedArea() == null
                                    || shellPane.getConsolePane().getFocusedArea() != null
                                            && shellPane.getConsolePane().getFocusedArea().getLength() == 0,
                            shellPane.getConsolePane().focusedAreaProperty(), inputArea.lengthProperty(),
                            outputArea.lengthProperty()));

            actions.redoEmptyProperty().bind(Bindings.createBooleanBinding(() -> !inputArea.isRedoAvailable(),
                    inputArea.redoAvailableProperty()));
            actions.undoEmptyProperty().bind(Bindings.createBooleanBinding(() -> !inputArea.isUndoAvailable(),
                    inputArea.undoAvailableProperty()));

            actions.historyStartReachedProperty().bind(shellPane.getConsolePane().historyStartReachedProperty());
            actions.historyEndReachedProperty().bind(shellPane.getConsolePane().historyEndReachedProperty());
        }
    }

    public void copy() {
        shellPane.getConsolePane().getFocusedArea().copy();
    }

    public void cut() {
        shellPane.getConsolePane().getFocusedArea().cut();
    }

    public void paste() {
        shellPane.getConsolePane().getFocusedArea().paste();
    }

    public void selectAll() {
        shellPane.getConsolePane().getFocusedArea().selectAll();
    }

    public void clear() {
        shellPane.getConsolePane().getFocusedArea().clear();
    }

    public void undo() {
        shellPane.getConsolePane().getFocusedArea().undo();
    }

    public void redo() {
        shellPane.getConsolePane().getFocusedArea().redo();
    }

    public void submit() {
        shellPane.submit();
    }

    public void submitLine() {
        shellPane.submitLine();
    }

    public void eval() {
        shellPane.eval();
    }

    public void evalLine() {
        shellPane.evalLine();
    }

    public void historyUp() {
        shellPane.getConsolePane().historyUp();
    }

    public void historyDown() {
        shellPane.getConsolePane().historyDown();
    }

    public void insertDirPath() {
        shellPane.insertDirPath();
    }

    public void insertFilePaths() {
        shellPane.insertFilePaths();
    }

    public void insertSaveFilePath() {
        shellPane.insertSaveFilePath();
    }

    public void showCodeCompletion() {
        shellPane.showCodeCompletion();
    }

    @Override
    public void saveFile() {
        save(shellPane);
    }

    @Override
    public void saveAsFile() {
        String output = shellPane.getConsolePane().getInputArea().getText();
        Path fileName = shellPane.getFXPath().getPath().getFileName();

        var path = FileDialogUtils.saveSourceJavaFile(shellPane.getScene().getWindow(), fileName);

        path.ifPresent(savePath -> TaskManager.get().executeSequentially(
                CTask.create(() -> Files.writeString(savePath, output)).onSucceeded(p -> shellPane.saved(p))));

    }

    @Override
    public void save(ContentPane contentPane) {
        var pane = (ShellPane) contentPane;
        String output = pane.getConsolePane().getInputArea().getText();
        Path path = pane.getFXPath().getPath();

        if (!path.isAbsolute()) {
            path = FileDialogUtils.saveSourceJavaFile(pane.getScene().getWindow(), path.getFileName()).orElse(null);
        }

        if (path != null) {
            var savePath = path;
            TaskManager.get().executeSequentially(
                    CTask.create(() -> Files.writeString(savePath, output)).onSucceeded(p -> pane.saved(p)));
        }
    }
}
