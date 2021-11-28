package dev.jshfx.base.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.FileManager;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class RootPane extends BorderPane {

    private static RootPane instance;

    private TabPane centerPane = new TabPane();
    private ConsolePane consolPane = new ConsolePane();
    private Actions actions;
    private ObjectProperty<ContentPane> contentPane = new SimpleObjectProperty<>();
    private ObjectProperty<EnvPane> envPane = new SimpleObjectProperty<>();

    public RootPane() {
        actions = new Actions(this);
        actions.setActions(this);
        actions.setReadOnlyContextMenu(consolPane.getArea());

        SplitPane splitPane = new SplitPane(centerPane, consolPane);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8f);

        setCenter(splitPane);
        setListeners();

        actions.getActionController().newShell();
        centerPane.setTabDragPolicy(TabDragPolicy.REORDER);
        instance = this;
    }

    public static RootPane get() {
        return instance;
    }

    public Actions getActions() {
        return actions;
    }

    public void setToolBar(ToolBar toolbar) {
        setTop(toolbar);
    }

    private void setListeners() {

        sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                n.focusOwnerProperty().addListener((vv, oo, nn) -> {

                    while (!(nn instanceof EnvPane) && nn != null) {
                        nn = nn.getParent();
                    }

                    if (nn instanceof EnvPane ePane) {
                        envPane.set(ePane);
                    }
                });
            }
        });

        centerPane.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                contentPane.set((ContentPane) n.getContent());
                contentPane.get().activate();
                consolPane.setContentPane(contentPane.get());
            } else {
                consolPane.setContentPane(null);
                actions.empty();
                FileManager.get().restoreOutput();
            }
        });

        centerPane.getTabs().addListener((Observable o) -> {
            if (centerPane.getTabs().isEmpty()) {
                actions.getActionController().closeFindDialog();
            }
        });
    }

    public EnvPane getEnvPane() {
        return envPane.get();
    }
    
    public ContentPane getContentPane() {
        return contentPane.get();
    }

    public ReadOnlyObjectProperty<ContentPane> contentPaneProperty() {
        return contentPane;
    }

    public ObservableList<Tab> getTabs() {
        return centerPane.getTabs();
    }

    public boolean exists(String name) {
        return centerPane.getTabs().stream().anyMatch(
                t -> ((ContentPane) t.getContent()).getFXPath().getPath().getFileName().toString().equals(name));
    }

    public List<Path> getNew(List<Path> paths) {
        List<Path> newPaths = paths.stream()
                .filter(p -> centerPane.getTabs().stream()
                        .noneMatch(t -> ((ContentPane) t.getContent()).getFXPath().getPath().equals(p)))
                .collect(Collectors.toList());

        if (newPaths.isEmpty()) {
            var tab = centerPane.getTabs().stream()
                    .filter(t -> ((ContentPane) t.getContent()).getFXPath().getPath().equals(paths.get(0))).findFirst()
                    .get();
            centerPane.getSelectionModel().select(tab);
        }

        return newPaths;
    }

    public List<ContentPane> getModified() {
        return centerPane.getTabs().stream().map(t -> (ContentPane) t.getContent()).filter(ContentPane::isModified)
                .collect(Collectors.toList());
    }

    public void add(List<ContentPane> contentPanes) {
        var tabs = contentPanes.stream().map(this::add).collect(Collectors.toList());

        if (!tabs.isEmpty()) {
            centerPane.getSelectionModel().select(tabs.get(0));
            tabs.get(0).getContent().requestFocus();
        }
    }

    public void addSelect(ContentPane contentPane) {
        var tab = add(contentPane);
        centerPane.getSelectionModel().select(tab);
    }

    private Tab add(ContentPane contentPane) {
        Tab tab = new Tab();
        actions.setTabContextMenu(tab);
        tab.setContent(contentPane);
        tab.setOnCloseRequest(e -> {
            centerPane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
            actions.getActionController().close(e);
            Platform.runLater(() -> centerPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER));
        });
        tab.textProperty().bind(contentPane.titleProperty());
        tab.setTooltip(new Tooltip());
        tab.getTooltip().textProperty().bind(contentPane.longTitleProperty());
        centerPane.getTabs().add(tab);

        contentPane.setOnCloseRequest(e -> actions.getActionController().close(tab));
        contentPane.init();

        return tab;
    }

    public void dispose() {
        actions.dispose();
        centerPane.getTabs().forEach(t -> ((ContentPane) t.getContent()).dispose());
        consolPane.dispose();
    }
}
