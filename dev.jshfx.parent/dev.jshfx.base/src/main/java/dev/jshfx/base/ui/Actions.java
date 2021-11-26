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

    private Action areaCopyAction;
    private Action areaCutAction;
    private Action areaPasteAction;
    private Action areaSelectAllAction;
    private Action areaClearAction;
    private Action areaUndoAction;
    private Action areaReduAction;

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

    private Consumer<ActionEvent> copyHandler;
    private Consumer<ActionEvent> cutHandler;
    private Consumer<ActionEvent> pasteHandler;
    private Consumer<ActionEvent> selectAllHandler;
    private Consumer<ActionEvent> clearHandler;
    private Consumer<ActionEvent> undoHandler;
    private Consumer<ActionEvent> redoHandler;
    private Consumer<ActionEvent> submitHandler;
    private Consumer<ActionEvent> submitLineHandler;
    private Consumer<ActionEvent> evalHandler;
    private Consumer<ActionEvent> evalLineHandler;
    private Consumer<ActionEvent> historyUpHandler;
    private Consumer<ActionEvent> historyDownHandler;
    private Consumer<ActionEvent> historySearchHandler;
    private Consumer<ActionEvent> insertDirPathHandler;
    private Consumer<ActionEvent> insertFilePathHandler;
    private Consumer<ActionEvent> insertSeparatedFilePathHandler;
    private Consumer<ActionEvent> insertSaveFilePathHandler;
    private Consumer<ActionEvent> codeCompletionHandler;
    private Consumer<ActionEvent> toggleCommentHandler;
    private Consumer<ActionEvent> saveSnapshotHandler;

    private BooleanExpression savedAllExpression;

    private BooleanProperty saved = new SimpleBooleanProperty();
    private BooleanProperty savedAll = new SimpleBooleanProperty();
    private BooleanProperty clipboardEmpty = new SimpleBooleanProperty();
    private BooleanProperty allSelected = new SimpleBooleanProperty();
    private BooleanProperty clear = new SimpleBooleanProperty();
    private BooleanProperty selectionEmpty = new SimpleBooleanProperty();
    private BooleanProperty redoEmpty = new SimpleBooleanProperty();
    private BooleanProperty undoEmpty = new SimpleBooleanProperty();
    private BooleanProperty historyStartReached = new SimpleBooleanProperty();
    private BooleanProperty historyEndReached = new SimpleBooleanProperty();

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
        saveAction.disabledProperty().bind(saved);

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
        areaCopyAction = new Action(e -> copyHandler.accept(e));
        areaCopyAction.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        FXResourceBundle.getBundle().put(areaCopyAction.textProperty(), "copy");
        FXResourceBundle.getBundle().put(areaCopyAction.longTextProperty(), "copy");
        areaCopyAction.disabledProperty().bind(selectionEmpty);

        areaCutAction = new Action(e -> cutHandler.accept(e));
        areaCutAction.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        FXResourceBundle.getBundle().put(areaCutAction.textProperty(), "cut");
        FXResourceBundle.getBundle().put(areaCutAction.longTextProperty(), "cut");
        areaCutAction.disabledProperty().bind(selectionEmpty);

        areaPasteAction = new Action(e -> pasteHandler.accept(e));
        areaPasteAction.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        FXResourceBundle.getBundle().put(areaPasteAction.textProperty(), "paste");
        FXResourceBundle.getBundle().put(areaPasteAction.longTextProperty(), "paste");
        areaPasteAction.disabledProperty().bind(clipboardEmpty);

        areaSelectAllAction = new Action(e -> selectAllHandler.accept(e));
        areaSelectAllAction.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        FXResourceBundle.getBundle().put(areaSelectAllAction.textProperty(), "selectAll");
        FXResourceBundle.getBundle().put(areaSelectAllAction.longTextProperty(), "selectAll");
        areaSelectAllAction.disabledProperty().bind(allSelected);

        areaClearAction = new Action(e -> clearHandler.accept(e));
        FXResourceBundle.getBundle().put(areaClearAction.textProperty(), "clear");
        FXResourceBundle.getBundle().put(areaClearAction.longTextProperty(), "clear");
        areaClearAction.disabledProperty().bind(clear);

        areaUndoAction = new Action(e -> undoHandler.accept(e));
        areaUndoAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        FXResourceBundle.getBundle().put(areaUndoAction.textProperty(), "undo");
        FXResourceBundle.getBundle().put(areaUndoAction.longTextProperty(), "undo");
        areaUndoAction.setDisabled(true);
        areaUndoAction.disabledProperty().bind(undoEmpty);

        areaReduAction = new Action(e -> redoHandler.accept(e));
        areaReduAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        FXResourceBundle.getBundle().put(areaReduAction.textProperty(), "redo");
        FXResourceBundle.getBundle().put(areaReduAction.longTextProperty(), "redo");
        areaReduAction.setDisabled(true);
        areaReduAction.disabledProperty().bind(redoEmpty);

        evalAction = new Action(e -> evalHandler.accept(e));
        evalAction.setGraphic(new StyleGlyph(Fonts.FONT_AWESOME_5_FREE_SOLID, Fonts.FontAwesome.PLAY));
        evalAction.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        FXResourceBundle.getBundle().put(evalAction.textProperty(), "evaluate");
        FXResourceBundle.getBundle().put(evalAction.longTextProperty(), "evaluateLong",
                evalAction.getAccelerator().getDisplayText());

        evalLineAction = new Action(e -> evalLineHandler.accept(e));
        evalLineAction.setGraphic(new StyleGlyph(Fonts.FONT_AWESOME_5_FREE_SOLID, Fonts.FontAwesome.ARROW_RIGHT));
        evalLineAction.setAccelerator(KeyCombination.keyCombination("Alt+E"));
        FXResourceBundle.getBundle().put(evalLineAction.textProperty(), "evaluateLine");
        FXResourceBundle.getBundle().put(evalLineAction.longTextProperty(), "evaluateLineLong",
                evalLineAction.getAccelerator().getDisplayText());

        submitAction = new Action(e -> submitHandler.accept(e));
        submitAction.setGraphic(new StyleGlyph(Fonts.MATERIAL_ICONS, Fonts.Material.SEND));
        submitAction.setAccelerator(KeyCombination.keyCombination("Shift+Enter"));
        FXResourceBundle.getBundle().put(submitAction.textProperty(), "submit");
        FXResourceBundle.getBundle().put(submitAction.longTextProperty(), "submitLong",
                submitAction.getAccelerator().getDisplayText());

        submitLineAction = new Action(e -> submitLineHandler.accept(e));
        submitLineAction.setGraphic(new StyleGlyph(Fonts.MATERIAL_ICONS, Fonts.Material.INPUT, 14));
        submitLineAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Enter"));
        FXResourceBundle.getBundle().put(submitLineAction.textProperty(), "submitLine");
        FXResourceBundle.getBundle().put(submitLineAction.longTextProperty(), "submitLineLong",
                submitLineAction.getAccelerator().getDisplayText());

        historyUpAction = new Action(e -> historyUpHandler.accept(e));
        FXResourceBundle.getBundle().put(historyUpAction.textProperty(), "historyUp");
        FXResourceBundle.getBundle().put(historyUpAction.longTextProperty(), "historyUp");
        historyUpAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Up"));
        historyUpAction.disabledProperty().bind(historyStartReached);

        historyDownAction = new Action(e -> historyDownHandler.accept(e));
        FXResourceBundle.getBundle().put(historyDownAction.textProperty(), "historyDown");
        FXResourceBundle.getBundle().put(historyDownAction.longTextProperty(), "historyDown");
        historyDownAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Down"));
        historyDownAction.disabledProperty().bind(historyEndReached);

        historySearchAction = new Action(e -> historySearchHandler.accept(e));
        FXResourceBundle.getBundle().put(historySearchAction.textProperty(), "historySearch");
        historySearchAction.setAccelerator(KeyCombination.keyCombination("Shortcut+R"));

        insertDirPathAction = new Action(e -> insertDirPathHandler.accept(e));
        FXResourceBundle.getBundle().put(insertDirPathAction.textProperty(), "insertDirPath");
        insertDirPathAction.setAccelerator(KeyCombination.keyCombination("Alt+D"));

        insertFilePathAction = new Action(e -> insertFilePathHandler.accept(e));
        FXResourceBundle.getBundle().put(insertFilePathAction.textProperty(), "insertFilePaths");
        insertFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+O"));
        
        insertSeparatedFilePathAction = new Action(e -> insertSeparatedFilePathHandler.accept(e));
        FXResourceBundle.getBundle().put(insertSeparatedFilePathAction.textProperty(), "insertSeparatedFilePaths");
        insertSeparatedFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+P"));

        insertSaveFilePathAction = new Action(e -> insertSaveFilePathHandler.accept(e));
        FXResourceBundle.getBundle().put(insertSaveFilePathAction.textProperty(), "insertSaveFilePath");
        insertSaveFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+S"));

        codeCompletionAction = new Action(e -> codeCompletionHandler.accept(e));
        FXResourceBundle.getBundle().put(codeCompletionAction.textProperty(), "codeCompletion");
        codeCompletionAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Space"));

        toggleCommentAction = new Action(e -> toggleCommentHandler.accept(e));
        FXResourceBundle.getBundle().put(toggleCommentAction.textProperty(), "toggleComment");
        toggleCommentAction.setAccelerator(KeyCombination.keyCombination("Shortcut+/"));

        saveSnapshotAction = new Action(e -> saveSnapshotHandler.accept(e));
        FXResourceBundle.getBundle().put(saveSnapshotAction.textProperty(), "save");

        findAction = new Action(e -> actionController.showFindDialog());
        FXResourceBundle.getBundle().put(findAction.textProperty(), "find");
        findAction.setAccelerator(KeyCombination.keyCombination("Shortcut+F"));

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

    public void setActions(ContentPane contentPane) {
        saveAsAction.setDisabled(false);
        if (savedAllExpression == null) {
            savedAllExpression = contentPane.modifiedProperty().not();
        } else {
            savedAllExpression = savedAllExpression.and(contentPane.modifiedProperty().not());
        }

        savedAll.bind(savedAllExpression);
    }

    public void setActions(EditorPane pane) {

        var menu = getContextMenu(pane.getArea());
        var actions = List.of(areaCopyAction, areaCutAction, areaPasteAction, areaSelectAllAction, areaClearAction,
                ActionUtils.ACTION_SEPARATOR, areaUndoAction, areaReduAction, ActionUtils.ACTION_SEPARATOR, findAction);
        ActionUtils.updateContextMenu(menu, actions);

        Nodes.addInputMap(pane.getArea(),
                sequence(
                        consume(keyPressed(saveAction.getAccelerator()).onlyIf(e -> !saveAction.isDisabled()),
                                e -> saveAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                        consume(keyPressed(saveAsAction.getAccelerator()).onlyIf(e -> !saveAsAction.isDisabled()),
                                e -> saveAsAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                        consume(keyPressed(findAction.getAccelerator()).onlyIf(e -> !findAction.isDisabled()),
                                e -> findAction.handle(new ActionEvent(e.getSource(), e.getTarget())))));
    }

    public void setActions(ShellPane shellPane) {
        setEditContextMenu(shellPane.getConsolePane().getInputArea());
        setReadOnlyContextMenu(shellPane.getConsolePane().getOutputArea());
    }

    public void bind(ContentPane contentPane) {
        saved.bind(contentPane.modifiedProperty().not());
    }

    public void bind(EditorPane pane) {
        WeakReference<EditorPane> paneRef = new WeakReference<>(pane);
        
        evalAction.setDisabled(true);
        evalLineAction.setDisabled(true);
        submitAction.setDisabled(true);
        submitLineAction.setDisabled(true);

        copyHandler = e -> paneRef.get().getArea().copy();
        cutHandler = e -> paneRef.get().getArea().cut();
        pasteHandler = e -> paneRef.get().getArea().paste();
        selectAllHandler = e -> paneRef.get().getArea().selectAll();
        clearHandler = e -> paneRef.get().getArea().clear();
        undoHandler = e -> paneRef.get().getArea().undo();
        redoHandler = e -> paneRef.get().getArea().redo();

        allSelected.bind(Bindings.createBooleanBinding(
                () -> pane.getArea().getSelectedText().length() == pane.getArea().getText().length(),
                pane.getArea().selectedTextProperty()));

        selectionEmpty.bind(Bindings.createBooleanBinding(
                () -> pane.getArea().getSelection().getLength() == 0,
                pane.getArea().selectionProperty()));

        clear.bind(Bindings.createBooleanBinding(
                () -> pane.getArea().getLength() == 0,
                pane.getArea().lengthProperty()));

        redoEmpty.bind(
                Bindings.createBooleanBinding(() -> !pane.getArea().isRedoAvailable(), pane.getArea().redoAvailableProperty()));
        undoEmpty.bind(
                Bindings.createBooleanBinding(() -> !pane.getArea().isUndoAvailable(), pane.getArea().undoAvailableProperty()));
    }

    public void bind(ShellPane shellPane) {
        WeakReference<ShellPane> paneRef = new WeakReference<>(shellPane);
        var inputArea = shellPane.getConsolePane().getInputArea();
        var outputArea = shellPane.getConsolePane().getOutputArea();

        evalAction.setDisabled(false);
        evalLineAction.setDisabled(false);
        submitAction.setDisabled(false);
        submitLineAction.setDisabled(false);

        copyHandler = e -> paneRef.get().getConsolePane().getFocusedArea().copy();
        cutHandler = e -> paneRef.get().getConsolePane().getFocusedArea().cut();
        pasteHandler = e -> paneRef.get().getConsolePane().getFocusedArea().paste();
        selectAllHandler = e -> paneRef.get().getConsolePane().getFocusedArea().selectAll();
        clearHandler = e -> paneRef.get().getConsolePane().getFocusedArea().clear();
        undoHandler = e -> paneRef.get().getConsolePane().getFocusedArea().undo();
        redoHandler = e -> paneRef.get().getConsolePane().getFocusedArea().redo();
        submitHandler = e -> paneRef.get().submit();
        submitLineHandler = e -> paneRef.get().submitLine();
        evalHandler = e -> paneRef.get().eval();
        evalLineHandler = e -> paneRef.get().evalLine();
        historyUpHandler = e -> paneRef.get().getConsolePane().historyUp();
        historyDownHandler = e -> paneRef.get().getConsolePane().historyDown();
        historySearchHandler = e -> paneRef.get().showHistorySearch();
        insertDirPathHandler = e -> paneRef.get().insertDirPath();
        insertFilePathHandler = e -> paneRef.get().insertFilePaths();
        insertSeparatedFilePathHandler = e -> paneRef.get().insertFilePaths(File.pathSeparator);
        insertSaveFilePathHandler = e -> paneRef.get().insertSaveFilePath();
        codeCompletionHandler = e -> paneRef.get().showCodeCompletion();
        toggleCommentHandler = e -> paneRef.get().toggleComment();

        allSelected.bind(Bindings.createBooleanBinding(
                () -> shellPane.getConsolePane().getFocusedArea() == null
                        || shellPane.getConsolePane().getFocusedArea() != null
                                && shellPane.getConsolePane().getFocusedArea().getSelectedText().length() == shellPane
                                        .getConsolePane().getFocusedArea().getText().length(),
                shellPane.getConsolePane().focusedAreaProperty(), inputArea.selectedTextProperty(),
                outputArea.selectedTextProperty()));

        selectionEmpty.bind(Bindings.createBooleanBinding(
                () -> shellPane.getConsolePane().getFocusedArea() == null
                        || shellPane.getConsolePane().getFocusedArea() != null
                                && shellPane.getConsolePane().getFocusedArea().getSelection().getLength() == 0,
                shellPane.getConsolePane().focusedAreaProperty(), inputArea.selectionProperty(),
                outputArea.selectionProperty()));

        clear.bind(Bindings.createBooleanBinding(
                () -> shellPane.getConsolePane().getFocusedArea() == null
                        || shellPane.getConsolePane().getFocusedArea() != null
                                && shellPane.getConsolePane().getFocusedArea().getLength() == 0,
                shellPane.getConsolePane().focusedAreaProperty(), inputArea.lengthProperty(),
                outputArea.lengthProperty()));

        redoEmpty.bind(
                Bindings.createBooleanBinding(() -> !inputArea.isRedoAvailable(), inputArea.redoAvailableProperty()));
        undoEmpty.bind(
                Bindings.createBooleanBinding(() -> !inputArea.isUndoAvailable(), inputArea.undoAvailableProperty()));

        historyStartReached.bind(shellPane.getConsolePane().historyStartReachedProperty());
        historyEndReached.bind(shellPane.getConsolePane().historyEndReachedProperty());
    }

    private void setEditContextMenu(GenericStyledArea<?, ?, ?> area) {
        var menu = getContextMenu(area);
        var actions = List.of(areaCopyAction, areaCutAction, areaPasteAction, areaSelectAllAction, areaClearAction,
                ActionUtils.ACTION_SEPARATOR, areaUndoAction, areaReduAction, ActionUtils.ACTION_SEPARATOR, evalAction,
                evalLineAction, submitAction, submitLineAction, ActionUtils.ACTION_SEPARATOR, historyUpAction,
                historyDownAction, ActionUtils.ACTION_SEPARATOR, insertDirPathAction, insertFilePathAction, insertSeparatedFilePathAction,
                insertSaveFilePathAction, ActionUtils.ACTION_SEPARATOR, codeCompletionAction, historySearchAction,
                findAction, ActionUtils.ACTION_SEPARATOR, toggleCommentAction);
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

    private void setReadOnlyContextMenu(GenericStyledArea<?, ?, ?> area) {
        var menu = getContextMenu(area);
        var actions = List.of(areaCopyAction, areaSelectAllAction, areaClearAction);
        ActionUtils.updateContextMenu(menu, actions);
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
