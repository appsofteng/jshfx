package dev.jshfx.base.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.FileManager;
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
              FileManager.get().restoreOutput();
            }
        });
    }

    public List<Path> getNew(List<Path> paths) {
        List<Path> newPaths = paths.stream()
                .filter(p -> centerPane.getTabs().stream()
                        .filter(t -> t.getContent() instanceof PathPane)
                        .noneMatch(t -> ((PathPane) t.getContent()).getPath().getPath().equals(p)))
                .collect(Collectors.toList());
        
        if (newPaths.isEmpty()) {
            var tab = centerPane.getTabs().stream()
            .filter(t -> t.getContent() instanceof PathPane)
            .filter(t -> ((PathPane) t.getContent()).getPath().getPath().equals(paths.get(0)))
            .findFirst().get();
            centerPane.getSelectionModel().select(tab);
        }

        return newPaths;
    }

    public void add(List<ContentPane> contentPanes) {
        var tabs = contentPanes.stream().map(this::add).collect(Collectors.toList());
        
        if (!tabs.isEmpty()) {
            centerPane.getSelectionModel().select(tabs.get(0));
        }
    }
    
    public void addSelect(ContentPane contentPane) {
        var tab = add(contentPane);
        centerPane.getSelectionModel().select(tab);
    }
    
    public Tab add(ContentPane contentPane) {
        actions.init(contentPane);
        Tab tab = new Tab();
        tab.setContent(contentPane);
        tab.setOnClosed(e -> contentPane.dispose());
        tab.textProperty().bind(contentPane.titleProperty());
        tab.setTooltip(new Tooltip());
        tab.getTooltip().textProperty().bind(contentPane.longTitleProperty());
        centerPane.getTabs().add(tab);

        contentPane.closedProperty().addListener((b, o, n) -> {
            if (n) {

                Platform.runLater(() -> centerPane.getTabs().remove(tab));
            }
        });
        
        return tab;
    }

    public void dispose() {
        actions.dispose();
        centerPane.getTabs().forEach(t -> ((ContentPane) t.getContent()).dispose());
    }
}
