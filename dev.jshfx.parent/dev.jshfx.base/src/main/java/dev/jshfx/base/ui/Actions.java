package dev.jshfx.base.ui;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.List;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.wellbehaved.event.Nodes;

import dev.jshfx.base.sys.ResourceManager;
import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class Actions {

    private static final Actions INSTANCE = new Actions();

    private BindingManager bindingManager;
    private ActionController actionController;
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

    public static Actions get() {
        return INSTANCE;
    }

    private Actions() {
    }

    public void init(BindingManager bindingManager, ActionController actionController) {
        this.bindingManager = bindingManager;
        this.actionController = actionController;

        areaCopyAction = new Action(e -> actionController.copy());
        areaCopyAction.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        FXResourceBundle.getBundle().put(areaCopyAction.textProperty(), "copy");
        FXResourceBundle.getBundle().put(areaCopyAction.longTextProperty(), "copy");
        areaCopyAction.disabledProperty().bind(bindingManager.selectionEmptyProperty());

        areaCutAction = new Action(e -> actionController.cut());
        areaCutAction.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        FXResourceBundle.getBundle().put(areaCutAction.textProperty(), "cut");
        FXResourceBundle.getBundle().put(areaCutAction.longTextProperty(), "cut");
        areaCutAction.disabledProperty().bind(bindingManager.selectionEmptyProperty());

        areaPasteAction = new Action(e -> actionController.paste());
        areaPasteAction.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        FXResourceBundle.getBundle().put(areaPasteAction.textProperty(), "paste");
        FXResourceBundle.getBundle().put(areaPasteAction.longTextProperty(), "paste");
        areaPasteAction.disabledProperty().bind(bindingManager.clipboardEmptyProperty());

        areaSelectAllAction = new Action(e -> actionController.selectAll());
        areaSelectAllAction.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        FXResourceBundle.getBundle().put(areaSelectAllAction.textProperty(), "selectAll");
        FXResourceBundle.getBundle().put(areaSelectAllAction.longTextProperty(), "selectAll");
        areaSelectAllAction.disabledProperty().bind(bindingManager.allSelectedProperty());

        areaClearAction = new Action(e -> actionController.clear());
        FXResourceBundle.getBundle().put(areaClearAction.textProperty(), "clear");
        FXResourceBundle.getBundle().put(areaClearAction.longTextProperty(), "clear");
        areaClearAction.disabledProperty().bind(bindingManager.clearProperty());

        areaUndoAction = new Action(e -> actionController.undo());
        areaUndoAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        FXResourceBundle.getBundle().put(areaUndoAction.textProperty(), "undo");
        FXResourceBundle.getBundle().put(areaUndoAction.longTextProperty(), "undo");
        areaUndoAction.setDisabled(true);
        areaUndoAction.disabledProperty().bind(bindingManager.undoEmptyProperty());

        areaReduAction = new Action(e -> actionController.redo());
        areaReduAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        FXResourceBundle.getBundle().put(areaReduAction.textProperty(), "redo");
        FXResourceBundle.getBundle().put(areaReduAction.longTextProperty(), "redo");
        areaReduAction.setDisabled(true);
        areaReduAction.disabledProperty().bind(bindingManager.redoEmptyProperty());

        submitAction = new Action(e -> actionController.submit());
        FXResourceBundle.getBundle().put(submitAction.textProperty(), "submit");
        FXResourceBundle.getBundle().put(submitAction.longTextProperty(), "submit");
        submitAction.setAccelerator(KeyCombination.keyCombination("Shift+Enter"));

        submitLineAction = new Action(e -> actionController.submitLine());
        FXResourceBundle.getBundle().put(submitLineAction.textProperty(), "submitLine");
        FXResourceBundle.getBundle().put(submitLineAction.longTextProperty(), "submitLine");
        submitLineAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Enter"));

        evalAction = new Action(e -> actionController.eval());
        FXResourceBundle.getBundle().put(evalAction.textProperty(), "evaluate");
        FXResourceBundle.getBundle().put(evalAction.longTextProperty(), "evaluate");
        evalAction.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));

        evalLineAction = new Action(e -> actionController.evalLine());
        FXResourceBundle.getBundle().put(evalLineAction.textProperty(), "evaluateLine");
        FXResourceBundle.getBundle().put(evalLineAction.longTextProperty(), "evaluateLine");
        evalLineAction.setAccelerator(KeyCombination.keyCombination("Alt+E"));

        historyUpAction = new Action(e -> actionController.historyUp());
        FXResourceBundle.getBundle().put(historyUpAction.textProperty(), "historyUp");
        FXResourceBundle.getBundle().put(historyUpAction.longTextProperty(), "historyUp");
        historyUpAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Up"));
        historyUpAction.disabledProperty().bind(bindingManager.historyStartReachedProperty());

        historyDownAction = new Action(e -> actionController.historyDown());
        FXResourceBundle.getBundle().put(historyDownAction.textProperty(), "historyDown");
        FXResourceBundle.getBundle().put(historyDownAction.longTextProperty(), "historyDown");
        historyDownAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Down"));
        historyDownAction.disabledProperty().bind(bindingManager.historyEndReachedProperty());

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
        codeCompletionAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Space"));

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
        saveAction.setDisabled(true);
        FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
        FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "save");
        saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        
        saveAsAction = new Action(e -> actionController.saveAsFile());
        saveAsAction.setGraphic(new ImageView(ResourceManager.get().getImage("baseline_save_as_black_24dp.png", 16, 16, true, false)));
        saveAsAction.setDisabled(true);
        FXResourceBundle.getBundle().put(saveAsAction.textProperty(), "saveAs");
        FXResourceBundle.getBundle().put(saveAsAction.longTextProperty(), "saveAs");
        saveAsAction.setAccelerator(KeyCombination.keyCombination("Shortcut+Alt+S"));
        
        saveAllAction = new Action(e -> actionController.saveAll());
        saveAllAction.setGraphic(new ImageView(ResourceManager.get().getImage("save-all.png")));
        saveAllAction.setDisabled(true);
        FXResourceBundle.getBundle().put(saveAllAction.textProperty(), "saveAll");
        FXResourceBundle.getBundle().put(saveAllAction.longTextProperty(), "saveAll");
        saveAllAction.setAccelerator(KeyCombination.keyCombination("Shift+Shortcut+S"));
    }

    public ToolBar getToolbar() {
        return ActionUtils.createToolBar(List.of(newAction, openAction, saveAction, saveAsAction, saveAllAction), ActionTextBehavior.HIDE);
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
