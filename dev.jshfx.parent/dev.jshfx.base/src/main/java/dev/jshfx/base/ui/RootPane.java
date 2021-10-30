package dev.jshfx.base.ui;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class RootPane extends BorderPane {

    private TabPane centerPane;
    private Actions actions;

    public RootPane() {
        centerPane = new TabPane();
        actions = new Actions(this);
        centerPane.setTabDragPolicy(TabDragPolicy.REORDER);
        setTop(actions.getToolbar());
        setCenter(centerPane);
        setListeners();

        actions.getActionController().newShell();
    }

    private void setListeners() {
        centerPane.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                ContentPane contentPane = (ContentPane) n.getContent();
                contentPane.activate();
                actions.bind(contentPane);
            } else {
                System.setErr(null);
                System.setOut(null);
            }
        });
    }

    public void newTab(ContentPane contentPane) {
        actions.init(contentPane);
        Tab tab = new Tab();
        tab.setContent(contentPane);
        tab.setOnClosed(e -> contentPane.dispose());
        tab.textProperty().bind(contentPane.titleProperty());
        tab.setTooltip(new Tooltip());
        tab.getTooltip().textProperty().bind(contentPane.longTitleProperty());
        centerPane.getTabs().add(tab);
        tab.getTabPane().getSelectionModel().select(tab);
        
        contentPane.closedProperty().addListener((b, o, n) -> {
            if (n) {

                Platform.runLater(() -> centerPane.getTabs().remove(tab));
            }
        });
    }

    public void dispose() {
        actions.dispose();
        centerPane.getTabs().forEach(t -> ((ContentPane) t.getContent()).dispose());
    }
}
