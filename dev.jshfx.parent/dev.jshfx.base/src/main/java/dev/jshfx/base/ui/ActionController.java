package dev.jshfx.base.ui;

public class ActionController {

    private RootPane rootPane;

    public ActionController(RootPane rootPane) {
        this.rootPane = rootPane;
    }
    
    public void copy() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.copy();
        }
    }
    
    public void cut() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.cut();
        }
    }
    
    public void paste() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.paste();
        }
    }
    
    public void selectAll() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.selectAll();
        }
    }
    
    public void clear() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.clear();
        }
    }
    
    public void undo() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.undo();
        }
    }
    
    public void redo() {
        var area = rootPane.getSelectedShell().getConsolePane().getFocusedArea();
        if (area != null) {
            area.redo();
        }
    }
    
    public void submit() {
        rootPane.getSelectedShell().submit();
    }
    
    public void submitLine() {
        rootPane.getSelectedShell().submitLine();
    }
    
    public void eval() {
        rootPane.getSelectedShell().eval();
    }
    
    public void evalLine() {
        rootPane.getSelectedShell().evalLine();
    }
    
    public void historyUp() {
        rootPane.getSelectedShell().getConsolePane().historyUp();
    }
    
    public void historyDown() {
        rootPane.getSelectedShell().getConsolePane().historyDown();
    }
    
    public void insertDirPath() {
        rootPane.getSelectedShell().insertDirPath();
    }
    
    public void insertFilePaths() {
        rootPane.getSelectedShell().insertFilePaths();
    }
    
    public void insertSaveFilePath() {
        rootPane.getSelectedShell().insertSaveFilePath();
    }
    
    public void showCodeCompletion() {
        rootPane.getSelectedShell().showCodeCompletion();
    }
    
    public void newShell() {
        rootPane.newShell();
    }
    
    public void openFile() {

    }

    public void saveFile() {

    }
    
    public void saveAsFile() {

    }
    
    public void saveAll() {

    }
}
