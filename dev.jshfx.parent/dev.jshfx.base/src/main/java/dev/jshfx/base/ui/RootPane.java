package dev.jshfx.base.ui;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class RootPane extends BorderPane {

	private TabPane centerPane;
	private ObjectProperty<ShellPane> selectedShell = new SimpleObjectProperty<>();
	private ObjectProperty<GenericStyledArea<?, ?, ?>> inputArea = new SimpleObjectProperty<>();
	private ObjectProperty<GenericStyledArea<?, ?, ?>> outputArea = new SimpleObjectProperty<>();

	public RootPane() {
		centerPane = new TabPane();
		centerPane.setTabDragPolicy(TabDragPolicy.REORDER);
		
		Actions.get().init(this);

		ToolBar toolBar = Actions.get().getToolbar();

		setTop(toolBar);		
		setCenter(centerPane);
		setListeners();
		
		newShell();
	}
	
	private void setListeners() {
		centerPane.getSelectionModel().selectedItemProperty().addListener((v,o,n) -> {
			if (n != null) {
				selectedShell.set((ShellPane) n.getContent());
				selectedShell.get().getSession().setIO();
				inputArea.set(selectedShell.get().getConsolePane().getInputArea());
				outputArea.set(selectedShell.get().getConsolePane().getOutputArea());
			} else {
				selectedShell.set(null);
				inputArea.set(null);
				outputArea.set(null);
		        System.setErr(null);
		        System.setOut(null);
			}
		});
	}
	
	public ShellPane getSelectedShell() {
		return selectedShell.get();
	}
	
	public ReadOnlyObjectProperty<ShellPane> selectedShellProperty() {
		return selectedShell;
	}
	
	public ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> inputAreaProperty() {
		return inputArea;
	}
	
	public ReadOnlyObjectProperty<GenericStyledArea<?, ?, ?>> outputAreaProperty() {
		return outputArea;
	}
	
	public void newShell() {
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
