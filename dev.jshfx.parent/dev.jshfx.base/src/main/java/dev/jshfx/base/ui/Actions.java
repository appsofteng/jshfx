package dev.jshfx.base.ui;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.wellbehaved.event.Nodes;

import dev.jshfx.base.sys.ResourceManager;
import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class Actions {

    private RootPane rootPane;
    private ScheduledService<Void> clipboardService;
    private ActionController actionController;
    private Map<String, ActionController> actionControllers = new HashMap<>();

    private Action newAction;
    private Action openAction;
    private Action saveAction;
    private Action saveAsAction;
    private Action saveAllAction;
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

    private Action insertDirPathAction;
    private Action insertFilePathAction;
    private Action insertSaveFilePathAction;

    private Action codeCompletionAction;

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
        this.rootPane = rootPane;

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

        actionController = new ActionController(rootPane, this);
        actionControllers.put(Actions.class.getName(), actionController);

        areaCopyAction = new Action(e -> actionController.copy());
        areaCopyAction.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        FXResourceBundle.getBundle().put(areaCopyAction.textProperty(), "copy");
        FXResourceBundle.getBundle().put(areaCopyAction.longTextProperty(), "copy");
        areaCopyAction.disabledProperty().bind(selectionEmptyProperty());

        areaCutAction = new Action(e -> actionController.cut());
        areaCutAction.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        FXResourceBundle.getBundle().put(areaCutAction.textProperty(), "cut");
        FXResourceBundle.getBundle().put(areaCutAction.longTextProperty(), "cut");
        areaCutAction.disabledProperty().bind(selectionEmptyProperty());

        areaPasteAction = new Action(e -> actionController.paste());
        areaPasteAction.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        FXResourceBundle.getBundle().put(areaPasteAction.textProperty(), "paste");
        FXResourceBundle.getBundle().put(areaPasteAction.longTextProperty(), "paste");
        areaPasteAction.disabledProperty().bind(clipboardEmpty);

        areaSelectAllAction = new Action(e -> actionController.selectAll());
        areaSelectAllAction.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        FXResourceBundle.getBundle().put(areaSelectAllAction.textProperty(), "selectAll");
        FXResourceBundle.getBundle().put(areaSelectAllAction.longTextProperty(), "selectAll");
        areaSelectAllAction.disabledProperty().bind(allSelectedProperty());

        areaClearAction = new Action(e -> actionController.clear());
        FXResourceBundle.getBundle().put(areaClearAction.textProperty(), "clear");
        FXResourceBundle.getBundle().put(areaClearAction.longTextProperty(), "clear");
        areaClearAction.disabledProperty().bind(clearProperty());

        areaUndoAction = new Action(e -> actionController.undo());
        areaUndoAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        FXResourceBundle.getBundle().put(areaUndoAction.textProperty(), "undo");
        FXResourceBundle.getBundle().put(areaUndoAction.longTextProperty(), "undo");
        areaUndoAction.setDisabled(true);
        areaUndoAction.disabledProperty().bind(undoEmptyProperty());

        areaReduAction = new Action(e -> actionController.redo());
        areaReduAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        FXResourceBundle.getBundle().put(areaReduAction.textProperty(), "redo");
        FXResourceBundle.getBundle().put(areaReduAction.longTextProperty(), "redo");
        areaReduAction.setDisabled(true);
        areaReduAction.disabledProperty().bind(redoEmptyProperty());

        submitAction = new Action(e -> actionController.submit());
        FXResourceBundle.getBundle().put(submitAction.textProperty(), "submit");
        FXResourceBundle.getBundle().put(submitAction.longTextProperty(), "submit");
        submitAction.setAccelerator(KeyCombination.keyCombination("Shift+Enter"));

        submitLineAction = new Action(e -> actionController.submitLine());
        FXResourceBundle.getBundle().put(submitLineAction.textProperty(), "submitLine");
        FXResourceBundle.getBundle().put(submitLineAction.longTextProperty(), "submitLine");
        submitLineAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Enter"));

        evalAction = new Action(e -> actionController.eval());
        FXResourceBundle.getBundle().put(evalAction.textProperty(), "evaluate");
        FXResourceBundle.getBundle().put(evalAction.longTextProperty(), "evaluate");
        evalAction.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));

        evalLineAction = new Action(e -> actionController.evalLine());
        FXResourceBundle.getBundle().put(evalLineAction.textProperty(), "evaluateLine");
        FXResourceBundle.getBundle().put(evalLineAction.longTextProperty(), "evaluateLine");
        evalLineAction.setAccelerator(KeyCombination.keyCombination("Alt+E"));

        historyUpAction = new Action(e -> actionController.historyUp());
        FXResourceBundle.getBundle().put(historyUpAction.textProperty(), "historyUp");
        FXResourceBundle.getBundle().put(historyUpAction.longTextProperty(), "historyUp");
        historyUpAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Up"));
        historyUpAction.disabledProperty().bind(historyStartReachedProperty());

        historyDownAction = new Action(e -> actionController.historyDown());
        FXResourceBundle.getBundle().put(historyDownAction.textProperty(), "historyDown");
        FXResourceBundle.getBundle().put(historyDownAction.longTextProperty(), "historyDown");
        historyDownAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Down"));
        historyDownAction.disabledProperty().bind(historyEndReachedProperty());

        insertDirPathAction = new Action(e -> actionController.insertDirPath());
        FXResourceBundle.getBundle().put(insertDirPathAction.textProperty(), "insertDirPath");
        FXResourceBundle.getBundle().put(insertDirPathAction.longTextProperty(), "insertDirPath");
        insertDirPathAction.setAccelerator(KeyCombination.keyCombination("Alt+D"));

        insertFilePathAction = new Action(e -> actionController.insertFilePaths());
        FXResourceBundle.getBundle().put(insertFilePathAction.textProperty(), "insertFilePaths");
        FXResourceBundle.getBundle().put(insertFilePathAction.longTextProperty(), "insertFilePaths");
        insertFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+O"));

        insertSaveFilePathAction = new Action(e -> actionController.insertSaveFilePath());
        FXResourceBundle.getBundle().put(insertSaveFilePathAction.textProperty(), "insertSaveFilePath");
        FXResourceBundle.getBundle().put(insertSaveFilePathAction.longTextProperty(), "insertSaveFilePath");
        insertSaveFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+S"));

        codeCompletionAction = new Action(e -> actionController.showCodeCompletion());
        FXResourceBundle.getBundle().put(codeCompletionAction.textProperty(), "codeCompletion");
        FXResourceBundle.getBundle().put(codeCompletionAction.longTextProperty(), "codeCompletion");
        codeCompletionAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Space"));

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
        saveAction.setGraphic(
                GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_SOLID).create(Fonts.Unicode.FLOPPY_DISK).size(14));
        saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
        FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "saveLong",
                saveAction.getAccelerator().getDisplayText());
        saveAction.disabledProperty().bind(savedProperty());

        saveAsAction = new Action(e -> actionController.saveAsFile());
        saveAsAction.setGraphic(new ImageView(ResourceManager.get().getImage("save-as.png")));
        saveAsAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Alt+S"));
        FXResourceBundle.getBundle().put(saveAsAction.textProperty(), "saveAs");
        FXResourceBundle.getBundle().put(saveAsAction.longTextProperty(), "saveAsLong",
                saveAsAction.getAccelerator().getDisplayText());

        saveAllAction = new Action(e -> saveAll());
        saveAllAction.setGraphic(new ImageView(ResourceManager.get().getImage("save-all.png")));
        saveAllAction.setAccelerator(KeyCombination.keyCombination("Shift+Shortcut+S"));
        FXResourceBundle.getBundle().put(saveAllAction.textProperty(), "saveAll");
        FXResourceBundle.getBundle().put(saveAllAction.longTextProperty(), "saveAllLong",
                saveAllAction.getAccelerator().getDisplayText());
        saveAllAction.disabledProperty().bind(savedAllProperty());
    }

    public void empty() {
        saveAsAction.setDisabled(true);
    }
    
    public void unbind() {
        savedProperty().unbind();
        allSelectedProperty().unbind();
        clearProperty().unbind();
        selectionEmptyProperty().unbind();
        redoEmptyProperty().unbind();
        undoEmptyProperty().unbind();
        historyStartReachedProperty().unbind();
        historyEndReachedProperty().unbind();
    }

    public BooleanProperty savedProperty() {
        return saved;
    }
    
    public BooleanProperty savedAllProperty() {
        return savedAll;
    }

    public BooleanProperty allSelectedProperty() {
        return allSelected;
    }

    public BooleanProperty clearProperty() {
        return clear;
    }

    public BooleanProperty selectionEmptyProperty() {
        return selectionEmpty;
    }

    public BooleanProperty redoEmptyProperty() {
        return redoEmpty;
    }

    public BooleanProperty undoEmptyProperty() {
        return undoEmpty;
    }

    public BooleanProperty historyStartReachedProperty() {
        return historyStartReached;
    }

    public BooleanProperty historyEndReachedProperty() {
        return historyEndReached;
    }

    public ActionController getActionController() {
        return actionController;
    }
    
    public void init(ContentPane contentPane) {
        saveAsAction.setDisabled(false);
        if (savedAllExpression == null) {
            savedAllExpression = contentPane.modifiedProperty().not();
        } else {
            savedAllExpression = savedAllExpression.and(contentPane.modifiedProperty().not());
        }
        
        savedAll.bind(savedAllExpression);
        getActionController(contentPane).init(contentPane);
    }

    public void bind(ContentPane contentPane) {
        actionController = getActionController(contentPane);
        actionController.bind(contentPane);
    }

    private ActionController getActionController(ContentPane contentPane) {
        var controller = actionControllers.get(contentPane.getClass().getName());

        if (controller == null) {
            if (contentPane instanceof ShellPane shellPane) {
                controller = new ShellActionController(rootPane, this, shellPane);
                actionControllers.put(contentPane.getClass().getName(), controller);
            } else {
                controller = actionControllers.get(Actions.class.getName());
            }
        }

        return controller;
    }

    private void saveAll() {

        var modifiedPanes = rootPane.getModified();
        modifiedPanes.forEach(cp -> getActionController(cp).save(cp));

    }

    public void dispose() {
        clipboardService.cancel();
    }

    public ToolBar getToolbar() {
        ToolBar toolBar = ActionUtils.createToolBar(
                List.of(newAction, openAction, saveAction, saveAsAction, saveAllAction), ActionTextBehavior.HIDE);

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

    public void setEditContextMenu(GenericStyledArea<?, ?, ?> area) {
        var menu = getContextMenu(area);
        var actions = List.of(areaCopyAction, areaCutAction, areaPasteAction, areaSelectAllAction, areaClearAction,
                ActionUtils.ACTION_SEPARATOR, areaUndoAction, areaReduAction, ActionUtils.ACTION_SEPARATOR,
                submitAction, submitLineAction, evalAction, evalLineAction, ActionUtils.ACTION_SEPARATOR,
                historyUpAction, historyDownAction, ActionUtils.ACTION_SEPARATOR, insertDirPathAction,
                insertFilePathAction, insertSaveFilePathAction, ActionUtils.ACTION_SEPARATOR, codeCompletionAction);
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
                consume(keyPressed(insertSaveFilePathAction.getAccelerator())
                        .onlyIf(e -> !insertSaveFilePathAction.isDisabled()),
                        e -> insertSaveFilePathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
                consume(keyPressed(codeCompletionAction.getAccelerator())
                        .onlyIf(e -> !codeCompletionAction.isDisabled()),
                        e -> codeCompletionAction.handle(new ActionEvent(e.getSource(), e.getTarget())))));
    }

    public void setReadOnlyContextMenu(GenericStyledArea<?, ?, ?> area) {
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
