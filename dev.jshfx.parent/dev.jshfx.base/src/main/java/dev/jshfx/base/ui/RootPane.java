package dev.jshfx.base.ui;

import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

public class RootPane extends BorderPane {

	private Actions actions;
	private TabPane centerPane;

	public RootPane() {
		centerPane = new TabPane();
		centerPane.setTabDragPolicy(TabDragPolicy.REORDER);
		
		actions = new Actions(centerPane.getTabs());
		actions.newShell();
		ToolBar toolBar = ActionUtils.createToolBar(actions.getActions(), ActionTextBehavior.HIDE);

		setTop(toolBar);		
		setCenter(centerPane);
	}
	
	public void dispose() {
		centerPane.getTabs().forEach(t -> ((ShellPane)t.getContent()).dispose());
	}
}
