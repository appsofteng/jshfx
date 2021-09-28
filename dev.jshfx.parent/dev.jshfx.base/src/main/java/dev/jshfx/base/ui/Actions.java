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
import dev.jshfx.fxmisc.richtext.ContextMenuBuilder;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class Actions {

	private static final Actions INSTANCE = new Actions();

	private Action newAction;
	private Action openAction;
	private Action saveAction;

	public static Actions get() {
		return INSTANCE;
	}

	private Actions() {

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

	public void newShell(Runnable handler) {
		newAction = new Action(e -> handler.run());
		newAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FILE));
		FXResourceBundle.getBundle().put(newAction.textProperty(), "new");
		FXResourceBundle.getBundle().put(newAction.longTextProperty(), "new");
	}

	public ToolBar getToolbar() {
		return ActionUtils.createToolBar(List.of(newAction, openAction, saveAction), ActionTextBehavior.HIDE);
	}

	public Action createAction(GenericStyledArea<?, ?, ?> area, Runnable handler, String key, String accelerator) {
		Action action = new Action(e -> handler.run());
		FXResourceBundle.getBundle().put(action.textProperty(), key);
		FXResourceBundle.getBundle().put(action.longTextProperty(), key);
		action.setAccelerator(KeyCombination.keyCombination(accelerator));
		area.getContextMenu().getItems().add(ActionUtils.createMenuItem(action));

		return action;
	}

	public Action createAction(GenericStyledArea<?, ?, ?> area, Runnable handler, String key, String accelerator,
			BooleanExpression disabled) {
		Action action = new Action(e -> handler.run());
		FXResourceBundle.getBundle().put(action.textProperty(), key);
		FXResourceBundle.getBundle().put(action.longTextProperty(), key);
		action.setAccelerator(KeyCombination.keyCombination(accelerator));
		action.disabledProperty().bind(disabled);
		area.getContextMenu().getItems().add(ActionUtils.createMenuItem(action));

		return action;
	}

	public List<Action> setEditContextMenu(GenericStyledArea<?, ?, ?> area) {
		var menu = getContextMenu(area);
		var actions = List.of(copy(area), cut(area), paste(area), selectAll(area), clear(area),
				ActionUtils.ACTION_SEPARATOR, undo(area), redo(area));
		ActionUtils.updateContextMenu(menu, actions);

		return actions;
	}

	public List<Action> setReadOnlyContextMenu(GenericStyledArea<?, ?, ?> area) {
		var menu = getContextMenu(area);
		var actions = List.of(copy(area), selectAll(area), clear(area));
		ActionUtils.updateContextMenu(menu, actions);

		return actions;
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

	private Action copy(GenericStyledArea<?, ?, ?> area) {
		Action action = new Action(e -> area.copy());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
		FXResourceBundle.getBundle().put(action.textProperty(), "copy");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "copy");
		action.disabledProperty().bind(
				Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0, area.selectionProperty()));

		return action;
	}

	private Action cut(GenericStyledArea<?, ?, ?> area) {

		Action action = new Action(e -> area.cut());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
		FXResourceBundle.getBundle().put(action.textProperty(), "cut");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "cut");
		action.disabledProperty().bind(
				Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0, area.selectionProperty()));

		return action;
	}

	private Action paste(GenericStyledArea<?, ?, ?> area) {

		Action action = new Action(e -> area.paste());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
		FXResourceBundle.getBundle().put(action.textProperty(), "paste");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "paste");
		area.getContextMenu().setOnShowing(e -> {
			action.setDisabled(!Clipboard.getSystemClipboard().hasString());
		});

		return action;
	}

	private Action selectAll(GenericStyledArea<?, ?, ?> area) {
		Action action = new Action(e -> area.selectAll());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
		FXResourceBundle.getBundle().put(action.textProperty(), "selectAll");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "selectAll");
		action.disabledProperty().bind(Bindings.createBooleanBinding(
				() -> area.getSelectedText().length() == area.getText().length(), area.selectedTextProperty()));

		return action;
	}

	private Action clear(GenericStyledArea<?, ?, ?> area) {
		Action action = new Action(e -> area.clear());
		FXResourceBundle.getBundle().put(action.textProperty(), "clear");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "clear");
		action.disabledProperty()
				.bind(Bindings.createBooleanBinding(() -> area.getLength() == 0, area.lengthProperty()));

		return action;
	}

	private Action undo(GenericStyledArea<?, ?, ?> area) {
		Action action = new Action(e -> area.undo());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
		FXResourceBundle.getBundle().put(action.textProperty(), "undo");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "undo");
		action.setDisabled(true);
		area.getUndoManager().undoAvailableProperty()
				.addListener((v, o, n) -> action.setDisabled(n == null || !(Boolean) n));

		return action;
	}

	private Action redo(GenericStyledArea<?, ?, ?> area) {
		Action action = new Action(e -> area.redo());
		action.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
		FXResourceBundle.getBundle().put(action.textProperty(), "redo");
		FXResourceBundle.getBundle().put(action.longTextProperty(), "redo");
		action.setDisabled(true);
		area.getUndoManager().redoAvailableProperty()
				.addListener((v, o, n) -> action.setDisabled(n == null || !(Boolean) n));

		return action;
	}

	private void openFile() {

	}

	private void saveFile() {

	}
}
