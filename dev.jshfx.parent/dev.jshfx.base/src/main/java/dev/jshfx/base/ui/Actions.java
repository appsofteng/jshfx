package dev.jshfx.base.ui;

import java.util.List;

import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;

public class Actions {

	private List<Action> actions;
	private ObservableList<Tab> tabs;
	private Action newAction;
	private Action openAction;
	private Action saveAction;

	public Actions(ObservableList<Tab> tabs) {
		this.tabs = tabs;
		
		newAction = new Action(e -> newShell());
		newAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FILE));
		FXResourceBundle.getBundle().put(newAction.textProperty(), "new");
		FXResourceBundle.getBundle().put(newAction.longTextProperty(), "new");
		
		openAction = new Action(e -> openFile());
		openAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR)
				.create(Fonts.FontAwesome.FOLDER_OPEN));
		FXResourceBundle.getBundle().put(openAction.textProperty(), "open");
		FXResourceBundle.getBundle().put(openAction.longTextProperty(), "open");
		
		saveAction = new Action(e -> saveFile());
		saveAction.setGraphic(
				GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_SOLID).create(Fonts.Unicode.FLOPPY_DISK).size(14));
		saveAction.setDisabled(true);
		FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
		FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "save");
		saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));

		actions = List.of(newAction, openAction, saveAction);
		
		newShell();
	}

	private void newShell() {
		Tab tab = new Tab();
		var shell = new ShellPane();
		tab.setContent(shell);
		tab.setOnClosed(e -> shell.dispose());
		tab.textProperty().bind(shell.titleProperty());
		tab.setTooltip(new Tooltip());
		tab.getTooltip().textProperty().bind(shell.longTitleProperty());
		tabs.add(tab);
		tab.getTabPane().getSelectionModel().select(tab);

		shell.closedProperty().addListener((b, o, n) -> {
			if (n) {
				
				Platform.runLater(() -> tabs.remove(tab));
			}
		});
	}
	
	private void openFile() {

	}
	
	private void saveFile() {

	}

	List<Action> getActions() {
		return actions;
	}
}
