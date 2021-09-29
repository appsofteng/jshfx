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

import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class Actions {

	private static final Actions INSTANCE = new Actions();

	private RootPane rootPane;
	private Action newAction;
	private Action openAction;
	private Action saveAction;
	private Action inputAreaCopyAction;
	private Action inputAreaCutAction;
	private Action inputAreaPasteAction;
	private Action inputAreaSelectAllAction;
	private Action inputAreaClearAction;
	private Action inputAreaUndoAction;
	private Action inputAreaReduAction;
	private Action outputAreaCopyAction;
	private Action outputAreaSelectAllAction;
	private Action outputAreaClearAction;

	private Action submitAction;
	private Action historyUpAction;
	private Action historyDownAction;

	private Action insertDirPathAction;
	private Action insertFilePathAction;

	private Action codeCompletionAction;

	public static Actions get() {
		return INSTANCE;
	}

	private Actions() {
	}

	public void init(RootPane value) {
		this.rootPane = value;
		inputAreaCopyAction = copy(rootPane.inputAreaProperty());
		inputAreaCutAction = cut(rootPane.inputAreaProperty());
		inputAreaPasteAction = paste(rootPane.inputAreaProperty());
		inputAreaSelectAllAction = selectAll(rootPane.inputAreaProperty());
		inputAreaClearAction = clear(rootPane.inputAreaProperty());
		inputAreaUndoAction = undo(rootPane.inputAreaProperty());
		inputAreaReduAction = redo(rootPane.inputAreaProperty());

		outputAreaCopyAction = copy(rootPane.outputAreaProperty());
		outputAreaSelectAllAction = selectAll(rootPane.outputAreaProperty());
		outputAreaClearAction = clear(rootPane.outputAreaProperty());

		rootPane.inputAreaProperty().addListener((v, o, n) -> {
			if (n != null) {
				inputAreaCopyAction.disabledProperty().bind(
						Bindings.createBooleanBinding(() -> n.getSelection().getLength() == 0, n.selectionProperty()));

				inputAreaCutAction.disabledProperty().bind(
						Bindings.createBooleanBinding(() -> n.getSelection().getLength() == 0, n.selectionProperty()));

				n.getContextMenu().setOnShowing(e -> {
					inputAreaPasteAction.setDisabled(!Clipboard.getSystemClipboard().hasString());
				});

				inputAreaSelectAllAction.disabledProperty().bind(Bindings.createBooleanBinding(
						() -> n.getSelectedText().length() == n.getText().length(), n.selectedTextProperty()));

				inputAreaClearAction.disabledProperty()
						.bind(Bindings.createBooleanBinding(() -> n.getLength() == 0, n.lengthProperty()));
				n.getUndoManager().undoAvailableProperty()
						.addListener((vv, oo, nn) -> inputAreaUndoAction.setDisabled(nn == null || !(Boolean) nn));

				n.getUndoManager().redoAvailableProperty()
						.addListener((vv, oo, nn) -> inputAreaReduAction.setDisabled(nn == null || !(Boolean) nn));
			} else {
				inputAreaCopyAction.disabledProperty().unbind();
				inputAreaCutAction.disabledProperty().unbind();
				inputAreaPasteAction.disabledProperty().unbind();
				inputAreaSelectAllAction.disabledProperty().unbind();
				inputAreaClearAction.disabledProperty().unbind();
				inputAreaUndoAction.disabledProperty().unbind();
				inputAreaReduAction.disabledProperty().unbind();
			}
		});

		rootPane.outputAreaProperty().addListener((v, o, n) -> {
			if (n != null) {
				outputAreaCopyAction.disabledProperty().bind(
						Bindings.createBooleanBinding(() -> n.getSelection().getLength() == 0, n.selectionProperty()));

				outputAreaSelectAllAction.disabledProperty().bind(Bindings.createBooleanBinding(
						() -> n.getSelectedText().length() == n.getText().length(), n.selectedTextProperty()));

				outputAreaClearAction.disabledProperty()
						.bind(Bindings.createBooleanBinding(() -> n.getLength() == 0, n.lengthProperty()));
			} else {
				outputAreaCopyAction.disabledProperty().unbind();
				outputAreaSelectAllAction.disabledProperty().unbind();
				outputAreaClearAction.disabledProperty().unbind();
			}
		});

		rootPane.selectedShellProperty().addListener((v, o, n) -> {
			if (n != null) {
				historyUpAction.disabledProperty().bind(n.getConsolePane().historyStartReachedProperty());
				historyDownAction.disabledProperty().bind(n.getConsolePane().historyEndReachedProperty());
			} else {
				historyUpAction.disabledProperty().unbind();
				historyDownAction.disabledProperty().unbind();
			}
		});

		submitAction = new Action(e -> rootPane.getSelectedShell().getConsolePane().enter());
		FXResourceBundle.getBundle().put(submitAction.textProperty(), "submit");
		FXResourceBundle.getBundle().put(submitAction.longTextProperty(), "submit");
		submitAction.setAccelerator(KeyCombination.keyCombination("Shift+Enter"));

		historyUpAction = new Action(e -> rootPane.getSelectedShell().getConsolePane().historyUp());
		FXResourceBundle.getBundle().put(historyUpAction.textProperty(), "historyUp");
		FXResourceBundle.getBundle().put(historyUpAction.longTextProperty(), "historyUp");
		historyUpAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Up"));

		historyDownAction = new Action(e -> rootPane.getSelectedShell().getConsolePane().historyDown());
		FXResourceBundle.getBundle().put(historyDownAction.textProperty(), "historyDown");
		FXResourceBundle.getBundle().put(historyDownAction.longTextProperty(), "historyDown");
		historyDownAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Down"));

		insertDirPathAction = new Action(e -> rootPane.getSelectedShell().insertDirPath());
		FXResourceBundle.getBundle().put(insertDirPathAction.textProperty(), "insertDirPath");
		FXResourceBundle.getBundle().put(insertDirPathAction.longTextProperty(), "insertDirPath");
		insertDirPathAction.setAccelerator(KeyCombination.keyCombination("Alt+D"));

		insertFilePathAction = new Action(e -> rootPane.getSelectedShell().insertFilePaths());
		FXResourceBundle.getBundle().put(insertFilePathAction.textProperty(), "insertFilePaths");
		FXResourceBundle.getBundle().put(insertFilePathAction.longTextProperty(), "insertFilePaths");
		insertFilePathAction.setAccelerator(KeyCombination.keyCombination("Alt+F"));

		codeCompletionAction = new Action(e -> rootPane.getSelectedShell().showCodeCompletion());
		FXResourceBundle.getBundle().put(codeCompletionAction.textProperty(), "codeCompletion");
		FXResourceBundle.getBundle().put(codeCompletionAction.longTextProperty(), "codeCompletion");
		codeCompletionAction.setAccelerator(KeyCombination.keyCombination("Ctrl+Space"));

		newAction = new Action(e -> rootPane.newShell());
		newAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FILE));
		FXResourceBundle.getBundle().put(newAction.textProperty(), "new");
		FXResourceBundle.getBundle().put(newAction.longTextProperty(), "new");

		openAction = new Action(e -> openFile());
		openAction.setGraphic(
				GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FOLDER_OPEN));
		FXResourceBundle.getBundle().put(openAction.textProperty(), "open");
		FXResourceBundle.getBundle().put(openAction.longTextProperty(), "open");

		saveAction = new Action(e -> saveFile());
		saveAction.setGraphic(
				GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_SOLID).create(Fonts.Unicode.FLOPPY_DISK).size(14));
		saveAction.setDisabled(true);
		FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
		FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "save");
		saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
	}

	public ToolBar getToolbar() {
		return ActionUtils.createToolBar(List.of(newAction, openAction, saveAction), ActionTextBehavior.HIDE);
	}

	public void setEditContextMenu(GenericStyledArea<?, ?, ?> area) {
		var menu = getContextMenu(area);
		var actions = List.of(inputAreaCopyAction, inputAreaCutAction, inputAreaPasteAction, inputAreaSelectAllAction,
				inputAreaClearAction, ActionUtils.ACTION_SEPARATOR, inputAreaUndoAction, inputAreaReduAction,
				ActionUtils.ACTION_SEPARATOR, submitAction, historyUpAction, historyDownAction,
				ActionUtils.ACTION_SEPARATOR, insertDirPathAction, insertFilePathAction, ActionUtils.ACTION_SEPARATOR,
				codeCompletionAction);
		ActionUtils.updateContextMenu(menu, actions);

		Nodes.addInputMap(area, sequence(
				consume(keyPressed(submitAction.getAccelerator()).onlyIf(e -> !submitAction.isDisabled()),
						e -> submitAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
				consume(keyPressed(historyUpAction.getAccelerator()).onlyIf(e -> !historyUpAction.isDisabled()),
						e -> historyUpAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
				consume(keyPressed(historyDownAction.getAccelerator()).onlyIf(e -> !historyDownAction.isDisabled()),
						e -> historyDownAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
				consume(keyPressed(insertDirPathAction.getAccelerator()).onlyIf(e -> !insertDirPathAction.isDisabled()),
						e -> insertDirPathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
				consume(keyPressed(insertFilePathAction.getAccelerator())
						.onlyIf(e -> !insertFilePathAction.isDisabled()),
						e -> insertFilePathAction.handle(new ActionEvent(e.getSource(), e.getTarget()))),
				consume(keyPressed(codeCompletionAction.getAccelerator())
						.onlyIf(e -> !codeCompletionAction.isDisabled()),
						e -> codeCompletionAction.handle(new ActionEvent(e.getSource(), e.getTarget())))));
	}

	public void setReadOnlyContextMenu(GenericStyledArea<?, ?, ?> area) {
		var menu = getContextMenu(area);
		var actions = List.of(outputAreaCopyAction, outputAreaSelectAllAction, outputAreaClearAction);
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

	private Action copy(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {
		Action action = new Action(e -> area.get().copy());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
		FXResourceBundle.getBundle().put(action.textProperty(), "copy");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "copy");

		return action;
	}

	private Action cut(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {

		Action action = new Action(e -> area.get().cut());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
		FXResourceBundle.getBundle().put(action.textProperty(), "cut");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "cut");

		return action;
	}

	private Action paste(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {

		Action action = new Action(e -> area.get().paste());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
		FXResourceBundle.getBundle().put(action.textProperty(), "paste");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "paste");

		return action;
	}

	private Action selectAll(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {
		Action action = new Action(e -> area.get().selectAll());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
		FXResourceBundle.getBundle().put(action.textProperty(), "selectAll");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "selectAll");

		return action;
	}

	private Action clear(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {
		Action action = new Action(e -> area.get().clear());
		FXResourceBundle.getBundle().put(action.textProperty(), "clear");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "clear");

		return action;
	}

	private Action undo(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {
		Action action = new Action(e -> area.get().undo());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
		FXResourceBundle.getBundle().put(action.textProperty(), "undo");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "undo");
		action.setDisabled(true);

		return action;
	}

	private Action redo(ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> area) {
		Action action = new Action(e -> area.get().redo());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
		FXResourceBundle.getBundle().put(action.textProperty(), "redo");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "redo");
		action.setDisabled(true);

		return action;
	}

	private void openFile() {

	}

	private void saveFile() {

	}
}
