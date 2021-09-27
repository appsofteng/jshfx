package dev.jshfx.base.ui;

import java.util.List;

import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;

public class Actions {

	private List<Action> actions;
	private Action newAction;
	private ObservableList<Tab> tabs;

	public Actions(ObservableList<Tab> tabs) {
		this.tabs = tabs;
		newAction = new Action(e -> newShell());
		newAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_REGULAR).create(Fonts.FontAwesome.FILE));
		FXResourceBundle.getBundle().put(newAction.textProperty(), "new");
		FXResourceBundle.getBundle().put(newAction.longTextProperty(), "new");

		actions = List.of(newAction);
	}

	void newShell() {
		Tab tab = new Tab();
		var shell = new ShellPane();
		tab.setContent(shell);
		tab.setOnClosed(e -> shell.dispose());
		tabs.add(tab);
		tab.getTabPane().getSelectionModel().select(tab);

		shell.closedProperty().addListener((b, o, n) -> {
			if (n) {
				
				Platform.runLater(() -> tabs.remove(tab));
			}
		});
	}

	List<Action> getActions() {
		return actions;
	}
}
