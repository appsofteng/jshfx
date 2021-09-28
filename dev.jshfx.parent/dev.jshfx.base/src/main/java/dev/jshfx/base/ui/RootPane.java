package dev.jshfx.base.ui;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class RootPane extends BorderPane {

	private TabPane centerPane;

	public RootPane() {
		centerPane = new TabPane();
		centerPane.setTabDragPolicy(TabDragPolicy.REORDER);
		
		Actions.get().newShell(this::newShell);

		ToolBar toolBar = Actions.get().getToolbar();

		setTop(toolBar);		
		setCenter(centerPane);
		
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
		centerPane.getTabs().add(tab);
		tab.getTabPane().getSelectionModel().select(tab);

		shell.closedProperty().addListener((b, o, n) -> {
			if (n) {
				
				Platform.runLater(() -> centerPane.getTabs().remove(tab));
			}
		});
	}
	
	public void dispose() {
		centerPane.getTabs().forEach(t -> ((ShellPane)t.getContent()).dispose());
	}
}
