package dev.jshfx.base.ui;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.wellbehaved.event.Nodes;

import dev.jshfx.base.sys.ResourceManager;
import dev.jshfx.cfx.glyphfont.StyleGlyph;
import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.scene.NodeUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class Actions {

    private ScheduledService<Void> clipboardService;
    private ActionController actionController;

    private Action newAction;
    private Action openAction;
    private Action saveAction;
    private Action saveAsAction;
    private Action saveAllAction;

    private Action closeTabAction;
    private Action closeOtherTabsAction;
    private Action closeAllTabsAction;

    private Action copyAction;
    private Action cutAction;
    private Action pasteAction;
    private Action selectAllAction;
    private Action clearAction;
    private Action undoAction;
    private Action redoAction;

    private Action submitAction;
    private Action submitLineAction;
    private Action evalAction;
    private Action evalLineAction;
    private Action historyUpAction;
    private Action historyDownAction;
    private Action historySearchAction;

    private Action insertDirPathAction;
    private Action insertFilePathAction;
    private Action insertSeparatedFilePathAction;
    private Action insertSaveFilePathAction;

    private Action codeCompletionAction;
    private Action toggleCommentAction;
    private Action findAction;

    private Action saveSnapshotAction;

    private BooleanExpression savedAllExpression;

    private BooleanProperty savedAll = new SimpleBooleanProperty();
    private BooleanProperty clipboardEmpty = new SimpleBooleanProperty();
    private BooleanProperty allSelected = new SimpleBooleanProperty();
    private BooleanProperty clear = new SimpleBooleanProperty();
    private BooleanProperty selectionEmpty = new SimpleBooleanProperty();
    private BooleanProperty redoEmpty = new SimpleBooleanProperty();
    private BooleanProperty undoEmpty = new SimpleBooleanProperty();
    private BooleanProperty historyStartReached = new SimpleBooleanProperty();
    private BooleanProperty historyEndReached = new SimpleBooleanProperty();

    private Consumer<ActionEvent> saveSnapshotHandler;
    
    public Actions(RootPane rootPane) {

        clipboardService = new ScheduledService<>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {

                        Platform.runLater(() -> clipboardEmpty.set(!Clipboard.getSystemClipboard().hasString()));

                        return null;
                    }
                };
            }
        };
        clipboardService.setPeriod(Duration.seconds(1));

        actionController = new ActionController(rootPane);

        // Toolbar actions
        newAction = new Action(e -> actionController.newShell());
        newAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FILE));
        FXResourceBundle.getBundle().put(newAction.textProperty(), "new");
        FXResourceBundle.getBundle().put(newAction.longTextProperty(), "new");

        openAction = new Action(e -> actionController.openFile());
        openAction.setGraphic(
                GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FOLDER_OPEN));
        FXResourceBundle.getBundle().put(openAction.textProperty(), "open");
        FXResourceBundle.getBundle().put(openAction.longTextProperty(), "open");

        saveAction = new Action(e -> actionController.saveFile());
        saveAction.setGraphic(GlyphFontRegistry.font(Fonts.MATERIAL_ICONS).create(Fonts.Material.SAVE).size(16));
        saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
        FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "saveLong",
                saveAction.getAccelerator().getDisplayText());

        saveAsAction = new Action(e -> actionController.saveAsFile());
        saveAsAction.setGraphic(new ImageView(ResourceManager.get().getImage("save-as.png")));
        saveAsAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Alt+S"));
        FXResourceBundle.getBundle().put(saveAsAction.textProperty(), "saveAs");
        FXResourceBundle.getBundle().put(saveAsAction.longTextProperty(), "saveAsLong",
                saveAsAction.getAccelerator().getDisplayText());

        saveAllAction = new Action(e -> actionController.saveAll());
        saveAllAction.setGraphic(new ImageView(ResourceManager.get().getImage("save-all.png")));
        saveAllAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S"));
        FXResourceBundle.getBundle().put(saveAllAction.textProperty(), "saveAll");
        FXResourceBundle.getBundle().put(saveAllAction.longTextProperty(), "saveAllLong",
                saveAllAction.getAccelerator().getDisplayText());
        saveAllAction.disabledProperty().bind(savedAll);

        // Tab actions
        closeTabAction = new Action(e -> actionController.close(e));
        FXResourceBundle.getBundle().put(closeTabAction.textProperty(), "close");

        closeOtherTabsAction = new Action(e -> actionController.closeOthers(e));
        FXResourceBundle.getBundle().put(closeOtherTabsAction.textProperty(), "closeOthers");

        closeAllTabsAction = new Action(e -> actionController.closeAll());
        FXResourceBundle.getBundle().put(closeAllTabsAction.textProperty(), "closeAll");

        // Code area actions
        copyAction = new Action(e -> rootPane.getEnvPane().handle(copyAction));
        copyAction.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        FXResourceBundle.getBundle().put(copyAction.textProperty(), "copy");
        FXResourceBundle.getBundle().put(copyAction.longTextProperty(), "copy");
        copyAction.disabledProperty().bind(selectionEmpty);

        cutAction = new Action(e -> rootPane.getEnvPane().handle(cutAction));
        cutAction.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        FXResourceBundle.getBundle().put(cutAction.textProperty(), "cut");
        FXResourceBundle.getBundle().put(cutAction.longTextProperty(), "cut");
        cutAction.disabledProperty().bind(selectionEmpty);

        pasteAction = new Action(e -> rootPane.getEnvPane().handle(pasteAction));
        pasteAction.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        FXResourceBundle.getBundle().put(pasteAction.textProperty(), "paste");
        FXResourceBundle.getBundle().put(pasteAction.longTextProperty(), "paste");
        pasteAction.disabledProperty().bind(clipboardEmpty);

        selectAllAction = new Action(e -> rootPane.getEnvPane().handle(selectAllAction));
        selectAllAction.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        FXResourceBundle.getBundle().put(selectAllAction.textProperty(), "selectAll");
        FXResourceBundle.getBundle().put(selectAllAction.longTextProperty(), "selectAll");
        selectAllAction.disabledProperty().bind(allSelected);

        clearAction = new Action(e -> rootPane.getEnvPane().handle(clearAction));
        FXResourceBundle.getBundle().put(clearAction.textProperty(), "clear");
        FXResourceBundle.getBundle().put(clearAction.longTextProperty(), "clear");
        clearAction.disabledProperty().bind(clear);

        undoAction = new Action(e -> rootPane.getEnvPane().handle(undoAction));
        undoAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        FXResourceBundle.getBundle().put(undoAction.textProperty(), "undo");
        FXResourceBundle.getBundle().put(undoAction.longTextProperty(), "undo");
        undoAction.setDisabled(true);
        undoAction.disabledProperty().bind(undoEmpty);

        redoAction = new Action(e -> rootPane.getEnvPane().handle(redoAction));
        redoAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        FXResourceBundle.getBundle().put(redoAction.textProperty(), "redo");
        FXResourceBundle.getBundle().put(redoAction.longTextProperty(), "redo");
        redoAction.setDisabled(true);
        redoAction.disabledProperty().bind(redoEmpty);

        evalAction = new Action(e -> rootPane.getEnvPane().handle(evalAction));
        evalAction.setGraphic(new StyleGlyph(Fonts.FONT_AWESOME_5_FREE_SOLID, Fonts.FontAwesome.PLAY));
        evalAction.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        FXResourceBundle.getBundle().put(evalAction.textProperty(), "evaluate");
        FXResourceBundle.getBundle().put(evalAction.longTextProperty(), "evaluateLong",
                evalAction.getAccelerator().getDisplayText());

        evalLineAction = new Action(e -> rootPane.getEnvPane().handle(evalLineAction));
        evalLineAction.setGraphic(new StyleGlyph(Fonts.FONT_AWESOME_5_FREE_SOLID, Fonts.FontAwesome.ARROW_RIGHT));
        evalLineAction.setAccelerator(KeyCombination.keyCombination("Alt+E"));
        FXResourceBundle.getBundle().put(evalLineAction.textProperty(), "evaluateLine");
        FXResourceBundle.getBundle().put(evalLineAction.longTextProperty(), "evaluateLineLong",
                evalLineAction.getAccelerator().getDisplayText());

        submitAction = new Action(e -> rootPane.getEnvPane().handle(submitAction));
        submitAction.setGraphic(new StyleGlyph(Fonts.MATERIAL_ICONS, Fonts.Material.SEND));
        submitAction.setAccelerator(KeyCombination.keyCombination("Shift+Enter"));
        FXResourceBundle.getBundle().put(submitAction.textProperty(), "submit");
        FXResourceBundle.getBundle().put(submitAction.longTextProperty(), "submitLong",
                submitAction.getAccelerator().getDisplayText());

        submitLineAction = new Action(e -> rootPane.getEnvPane().handle(submitLineAction));
        submitLineAction.setGraphic(new StyleGlyph(Fonts.MATERIAL_ICONS, Fonts.Material.INPUT, 14));
        submitLineAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Enter"));
        FXResourceBundle.getBundle().put(submitLineAction.textProperty(), "submitLine");
        FXResourceBundle.getBundle().put(submitLineAction.longTextProperty(), "submitLineLong",
                submitLineAction.getAccelerator().getDisplayText());

        historyUpAction = new Action(e -> rootPane.getEnvPane().handle(historyUpAction));
        FXResourceBundle.getBundle().put(historyUpAction.textProperty(), "historyUp");
        FXResourceBundle.getBundle().put(historyUpAction.longTextProperty(), "historyUp");
        historyUpAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Up"));
        historyUpAction.disabledProperty().bind(historyStartReached);

        historyDownAction = new Action(e -> rootPane.getEnvPane().handle(historyDownAction));
        FXResourceBundle.getBundle().put(historyDownAction.textProperty(), "historyDown");
        FXResourceBundle.getBundle().put(historyDownAction.longTextProperty(), "historyDown");
        historyDownAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Down"));
        historyDownAction.disabledProperty().bind(historyEndReached);

        historySearchAction = new Action(e -> rootPane.getEnvPane().handle(historySearchAction));
        FXResourceBundle.getBundle().put(historySearchAction.textProperty(), "historySearch");
        historySearchAction.setAccelerator(KeyCombination.keyCombination("Shortcut+R"));

        insertDirPathAction = new Action(e -> rootPane.getEnvPane().handle(insertDirPathAction));
        FXResourceBundle.getBundle().put(insertDirPathAction.textProperty(), "insertDirPath");
        insertDirPathAction.setAccelerator(KeyCombination.keyCombination("Alt+D"));

        insertFilePathAction = new Action(e -> rootPane.getEnvPane().handle(insertFilePathAction));
        FXResourceBundle.getBundle().put(insertFilePathAction.textProperty(), "insertFilePaths");
        insertFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+O"));

        insertSeparatedFilePathAction = new Action(e -> rootPane.getEnvPane().handle(insertSeparatedFilePathAction));
        FXResourceBundle.getBundle().put(insertSeparatedFilePathAction.textProperty(), "insertSeparatedFilePaths");
        insertSeparatedFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+P"));

        insertSaveFilePathAction = new Action(e -> rootPane.getEnvPane().handle(insertSaveFilePathAction));
        FXResourceBundle.getBundle().put(insertSaveFilePathAction.textProperty(), "insertSaveFilePath");
        insertSaveFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+S"));

        codeCompletionAction = new Action(e -> rootPane.getEnvPane().handle(codeCompletionAction));
        FXResourceBundle.getBundle().put(codeCompletionAction.textProperty(), "codeCompletion");
        codeCompletionAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Space"));

        toggleCommentAction = new Action(e -> rootPane.getEnvPane().handle(toggleCommentAction));
        FXResourceBundle.getBundle().put(toggleCommentAction.textProperty(), "toggleComment");
        toggleCommentAction.setAccelerator(KeyCombination.keyCombination("Shortcut+/"));

        saveSnapshotAction = new Action(e -> saveSnapshotHandler.accept(e));
        FXResourceBundle.getBundle().put(saveSnapshotAction.textProperty(), "save");

        findAction = new Action(e -> actionController.showFindDialog());
        FXResourceBundle.getBundle().put(findAction.textProperty(), "find");
        findAction.setAccelerator(KeyCombination.keyCombination("Shortcut+F"));

    }
    
    public Action getCopyAction() {
        return copyAction;
    }
    
    public Action getCutAction() {
        return cutAction;
    }
    
    public Action getPasteAction() {
        return pasteAction;
    }

    public Action getSelectAllAction() {
        return selectAllAction;
    }
    
    public Action getClearAction() {
        return clearAction;
    }
    
    public Action getUndoAction() {
        return undoAction;
    }
    
    public Action getRedoAction() {
        return redoAction;
    }
    
    public Action getSubmitAction() {
        return submitAction;
    }
    
    public Action getSubmitLineAction() {
        return submitLineAction;
    }
    
    public Action getSaveAction() {
        return saveAction;
    }
    
    public ActionController getActionController() {
        return actionController;
    }

    public void empty() {
        saveAsAction.setDisabled(true);
        evalAction.setDisabled(true);
        evalLineAction.setDisabled(true);
        submitAction.setDisabled(true);
        submitLineAction.setDisabled(true);
    }

    public void dispose() {
        clipboardService.cancel();
    }

    public void setActions(RootPane rootPane) {
        rootPane.setToolBar(getToolbar());
        Nodes.addInputMap(rootPane,
                sequence(consume(keyPressed(saveAllAction.getAccelerator()).onlyIf(e -> !saveAllAction.isDisabled()),
                        e -> saveAllAction.handle(new ActionEvent(e.getSource(), e.getTarget())))));
        closeOtherTabsAction.disabledProperty().bind(Bindings.size(rootPane.getTabs()).isEqualTo(1));
    }
    
    public void setActions(ContentPane pane) {
        saveAsAction.setDisabled(false);
        if (savedAllExpression == null) {
            savedAllExpression = pane.modifiedProperty().not();
        } else {
            savedAllExpression = savedAllExpression.and(pane.modifiedProperty().not());
        }

        savedAll.bind(savedAllExpression);
    }

    private ToolBar getToolbar() {
        ToolBar toolBar = ActionUtils.createToolBar(List.of(newAction, openAction, saveAction, saveAsAction,
                saveAllAction, evalAction, evalLineAction, submitAction, submitLineAction), ActionTextBehavior.HIDE);

        toolBar.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {

            var node = toolBar.getItems().stream().filter(Node::isDisabled)
                    .filter(n -> n.contains(n.parentToLocal(e.getX(), e.getY()))).findFirst();

            if (node.isPresent() && node.get()instanceof Control control) {
                toolBar.setTooltip(control.getTooltip());
            } else {
                toolBar.setTooltip(null);
            }
        });

        return toolBar;
    }

    public void setTabContextMenu(Tab tab) {
        var menu = ActionUtils.createContextMenu(
                List.of(closeTabAction, ActionUtils.ACTION_SEPARATOR, closeOtherTabsAction, closeAllTabsAction));
        menu.setUserData(tab);
        tab.setContextMenu(menu);
    }

    public void setSnapshotContextMenu(Node node, String name) {
        var menu = ActionUtils.createContextMenu(List.of(saveSnapshotAction));

        saveSnapshotHandler = e -> {
            var path = FileDialogUtils.saveImageFile(node.getScene().getWindow(), Path.of(name));
            path.ifPresent(p -> NodeUtils.saveSnapshot(node, p));
        };

        node.setOnContextMenuRequested(e -> {
            menu.show(node, e.getScreenX(), e.getScreenY());
        });

        node.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                menu.hide();
            }
        });
    }
    
    public void setReadOnlyContextMenu(CodeArea area) {

        var menu = getContextMenu(area);
        var actions = List.of(copyAction, selectAllAction, clearAction);
        ActionUtils.updateContextMenu(menu, actions);
    }
    
    public void setEditorContextMenu(CodeArea area) {        
        var menu = getContextMenu(area);
        var actions = List.of(copyAction, cutAction, pasteAction, selectAllAction, clearAction,
                ActionUtils.ACTION_SEPARATOR, undoAction, redoAction, ActionUtils.ACTION_SEPARATOR, findAction);
        ActionUtils.updateContextMenu(menu, actions);

        Nodes.addInputMap(area,
                sequence(
                        consume(keyPressed(saveAction.getAccelerator()).onlyIf(e -> !saveAction.isDisabled()),
                                e -> saveAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                        consume(keyPressed(saveAsAction.getAccelerator()).onlyIf(e -> !saveAsAction.isDisabled()),
                                e -> saveAsAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                        consume(keyPressed(findAction.getAccelerator()).onlyIf(e -> !findAction.isDisabled()),
                                e -> findAction.handle(new ActionEvent(e.getSource(), e.getTarget())))));
    }

    public void setShellContextMenu(CodeArea area) {        
        var menu = getContextMenu(area);
        var actions = List.of(copyAction, cutAction, pasteAction, selectAllAction, clearAction,
                ActionUtils.ACTION_SEPARATOR, undoAction, redoAction, ActionUtils.ACTION_SEPARATOR, evalAction,
                evalLineAction, submitAction, submitLineAction, ActionUtils.ACTION_SEPARATOR, historyUpAction,
                historyDownAction, ActionUtils.ACTION_SEPARATOR, insertDirPathAction, insertFilePathAction,
                insertSeparatedFilePathAction, insertSaveFilePathAction, ActionUtils.ACTION_SEPARATOR,
                codeCompletionAction, historySearchAction, findAction, ActionUtils.ACTION_SEPARATOR,
                toggleCommentAction);
        ActionUtils.updateContextMenu(menu, actions);

        Nodes.addInputMap(area, sequence(
                consume(keyPressed(saveAction.getAccelerator()).onlyIf(e -> !saveAction.isDisabled()),
                        e -> saveAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(saveAsAction.getAccelerator()).onlyIf(e -> !saveAsAction.isDisabled()),
                        e -> saveAsAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(submitAction.getAccelerator()).onlyIf(e -> !submitAction.isDisabled()),
                        e -> submitAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(submitLineAction.getAccelerator()).onlyIf(e -> !submitLineAction.isDisabled()),
                        e -> submitLineAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(evalAction.getAccelerator()).onlyIf(e -> !evalAction.isDisabled()),
                        e -> evalAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(evalLineAction.getAccelerator()).onlyIf(e -> !evalLineAction.isDisabled()),
                        e -> evalLineAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(historyUpAction.getAccelerator()).onlyIf(e -> !historyUpAction.isDisabled()),
                        e -> historyUpAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(historyDownAction.getAccelerator()).onlyIf(e -> !historyDownAction.isDisabled()),
                        e -> historyDownAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(insertDirPathAction.getAccelerator()).onlyIf(e -> !insertDirPathAction.isDisabled()),
                        e -> insertDirPathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(insertFilePathAction.getAccelerator())
                        .onlyIf(e -> !insertFilePathAction.isDisabled()),
                        e -> insertFilePathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(insertSeparatedFilePathAction.getAccelerator())
                        .onlyIf(e -> !insertSeparatedFilePathAction.isDisabled()),
                        e -> insertSeparatedFilePathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(insertSaveFilePathAction.getAccelerator())
                        .onlyIf(e -> !insertSaveFilePathAction.isDisabled()),
                        e -> insertSaveFilePathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(codeCompletionAction.getAccelerator())
                        .onlyIf(e -> !codeCompletionAction.isDisabled()),
                        e -> codeCompletionAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(toggleCommentAction.getAccelerator()).onlyIf(e -> !toggleCommentAction.isDisabled()),
                        e -> toggleCommentAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(findAction.getAccelerator()).onlyIf(e -> !findAction.isDisabled()),
                        e -> findAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(historySearchAction.getAccelerator()).onlyIf(e -> !historySearchAction.isDisabled()),
                        e -> historySearchAction.handle(new ActionEvent(e.getSource(), e.getTarget())))));
    }

    public void bind(ConsolePane pane) {
        allSelected.bind(Bindings.createBooleanBinding(
                () -> pane.getArea().getSelectedText().length() == pane.getArea().getText().length(),
                pane.getArea().selectedTextProperty()));

        selectionEmpty.bind(Bindings.createBooleanBinding(() -> pane.getArea().getSelection().getLength() == 0,
                pane.getArea().selectionProperty()));

        clear.bind(
                Bindings.createBooleanBinding(() -> pane.getArea().getLength() == 0, pane.getArea().lengthProperty()));
    }
    
    public void bind(EditorPane pane) {
        evalAction.setDisabled(true);
        evalLineAction.setDisabled(true);
        submitAction.setDisabled(true);
        submitLineAction.setDisabled(true);

        allSelected.bind(Bindings.createBooleanBinding(
                () -> pane.getArea().getSelectedText().length() == pane.getArea().getText().length(),
                pane.getArea().selectedTextProperty()));

        selectionEmpty.bind(Bindings.createBooleanBinding(() -> pane.getArea().getSelection().getLength() == 0,
                pane.getArea().selectionProperty()));

        clear.bind(
                Bindings.createBooleanBinding(() -> pane.getArea().getLength() == 0, pane.getArea().lengthProperty()));

        redoEmpty.bind(Bindings.createBooleanBinding(() -> !pane.getArea().isRedoAvailable(),
                pane.getArea().redoAvailableProperty()));
        undoEmpty.bind(Bindings.createBooleanBinding(() -> !pane.getArea().isUndoAvailable(),
                pane.getArea().undoAvailableProperty()));
    }

    public void bind(ShellPane pane) {

        evalAction.setDisabled(false);
        evalLineAction.setDisabled(false);
        submitAction.setDisabled(false);
        submitLineAction.setDisabled(false);

        allSelected.bind(Bindings.createBooleanBinding(
                () -> pane.getArea().getSelectedText().length() == pane.getArea().getText().length(),
                pane.getArea().selectedTextProperty()));

        selectionEmpty.bind(Bindings.createBooleanBinding(() -> pane.getArea().getSelection().getLength() == 0,
                pane.getArea().selectionProperty()));

        clear.bind(Bindings.createBooleanBinding(() -> pane.getArea().getLength() == 0,
                pane.getArea().lengthProperty()));

        redoEmpty.bind(Bindings.createBooleanBinding(() -> !pane.getArea().isRedoAvailable(),
                pane.getArea().redoAvailableProperty()));
        undoEmpty.bind(Bindings.createBooleanBinding(() -> !pane.getArea().isUndoAvailable(),
                pane.getArea().undoAvailableProperty()));

        historyStartReached.bind(pane.historyStartReachedProperty());
        historyEndReached.bind(pane.historyEndReachedProperty());
    }

    private ContextMenu getContextMenu(GenericStyledArea<?, ?, ?> area) {
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);
        menu.setHideOnEscape(true);
        menu.setConsumeAutoHidingEvents(true);
        menu.addEventHandler(KeyEvent.ANY, e -> e.consume());
        menu.setOnShown(e -> area.requestFocus());
        menu.setOnHidden(e -> area.requestFocus());
        area.setContextMenu(menu);

        return menu;
    }
}
