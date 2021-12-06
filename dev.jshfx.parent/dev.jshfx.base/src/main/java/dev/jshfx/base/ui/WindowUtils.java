package dev.jshfx.base.ui;

import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.controlsfx.dialog.ProgressDialog;

import dev.jshfx.base.MainApp;
import dev.jshfx.base.sys.ResourceManager;
import dev.jshfx.jfx.scene.control.AutoCompleteArea;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.util.stage.WindowContent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class WindowUtils {

    private WindowUtils() {
    }

    public static void showProgress(Window window, Worker<?> worker) {
        var progressDialog = new ProgressDialog(worker);
        progressDialog.setHeaderText(null);
        progressDialog.initOwner(window);
        progressDialog.show();
    }

    public static void show(Window window, WindowContent windowContent) {

        Node content = windowContent.getNodes().get(0);

        if (windowContent.getNodes().size() > 1) {
            if (windowContent.getColumns() > 0) {
                content = getGridPane(windowContent);
            } else {
                content = getTabPane(windowContent);
            }
        }

        var name = windowContent.getTitle();

        if (name == null || name.isEmpty()) {
            name = FXResourceBundle.getBundle().getString​("untitled");
        }

        RootPane.get().getActions().setSnapshotContextMenu(content, name);

        BorderPane borderPane = new BorderPane(content);

        if (!windowContent.getTitle().isEmpty() && windowContent.getNodes().size() > 1) {
            var label = new Label(windowContent.getTitle());
            label.setFont(new Font(label.getFont().getName(), 20));
            borderPane.setTop(label);
            BorderPane.setAlignment(label, Pos.CENTER);
        }

        ScrollPane scrollPane = new ScrollPane(borderPane);
        scrollPane.setPrefSize(MainApp.WINDOW_PREF_WIDTH, MainApp.WINDOW_PREF_HEIGHT);

        scrollPane.viewportBoundsProperty().addListener((v, o, n) -> {
            borderPane.setMinSize(Math.max(borderPane.getPrefWidth(), n.getWidth()),
                    Math.max(borderPane.getPrefHeight(), n.getHeight()));
            scrollPane.setPannable(
                    (borderPane.getMinWidth() > n.getWidth()) || (borderPane.getMinHeight() > n.getHeight()));

        });

        Stage stage = new Stage();
        stage.setTitle(windowContent.getTitle());
        stage.getIcons().add(ResourceManager.get().getIconImage());
        stage.initOwner(window);
        Scene scene = new Scene(scrollPane);
        scene.getStylesheets().add(ResourceManager.get().getStyle());
        stage.setScene(scene);
        stage.show();
    }

    private static Node getTabPane(WindowContent windowContent) {

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        IntStream.range(0, windowContent.getNodes().size())
                .mapToObj(i -> new Tab(windowContent.getNodes().get(i).getId() == null ? "T" + i
                        : windowContent.getNodes().get(i).getId(), windowContent.getNodes().get(i)))
                .collect(Collectors.toCollection(() -> tabPane.getTabs()));

        return tabPane;
    }

    private static Node getGridPane(WindowContent windowContent) {
        int columns = windowContent.getColumns();
        int rows = windowContent.getNodes().size() / columns
                + (int) Math.signum(windowContent.getNodes().size() % columns);

        GridPane pane = new GridPane();

        for (int i = 0; i < windowContent.getNodes().size(); i++) {
            var chart = windowContent.getNodes().get(i);

            int column = i % columns;
            int row = (i + 1) / columns + (int) Math.signum((i + 1) % columns) - 1;

            GridPane.setRowIndex(chart, row);
            GridPane.setColumnIndex(chart, column);
        }

        while (columns-- > 0) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            pane.getColumnConstraints().add(column);
        }

        while (rows-- > 0) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            pane.getRowConstraints().add(row);
        }

        pane.getChildren().addAll(windowContent.getNodes());

        return pane;
    }

    public static void showHistorySearch(Window window, ObservableList<String> history, Consumer<String> onSelection) {
        Tooltip popup = new Tooltip();
        popup.setAutoHide(true);
        AutoCompleteArea<String> autoCompleteField = new AutoCompleteArea<>(new TreeSet<>(history));
        autoCompleteField.setOnAction(s -> {
            popup.hide();
            onSelection.accept(s);
        });
        autoCompleteField.setOnCancel(() -> {
            popup.hide();
        });
        autoCompleteField.setPrefWidth(window.getWidth() - 100);

        popup.setGraphic(autoCompleteField);
        popup.show(window);
    }
}
