package dev.jshfx.base.ui;

import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.controlsfx.dialog.ProgressDialog;

import dev.jshfx.jfx.scene.control.AutoCompleteArea;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.util.chart.Charts;
import dev.jshfx.util.control.Tables;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
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
import javafx.stage.Modality;
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

    public static void show(Window window, Object obj) {

        if (obj instanceof Charts charts) {

            Dialog<Void> dialog = createPlainDialog(window);
//            dialog.setWidth(MainApp.WINDOW_PREF_WIDTH);
//            dialog.setHeight(MainApp.WINDOW_PREF_HEIGHT);

            int columns = charts.getColumns();
            int rows = charts.getCharts().size() / columns + (int) Math.signum(charts.getCharts().size() % columns);

            GridPane chartPane = new GridPane();
            
            for (int i = 0; i < charts.getCharts().size(); i++) {
                var chart = charts.getCharts().get(i);
                
                int column = i % columns;
                int row = (i + 1) / columns + (int) Math.signum((i + 1) % columns) - 1;
                
                GridPane.setRowIndex(chart, row);
                GridPane.setColumnIndex(chart, column);
            }

            while (columns-- > 0) {
                ColumnConstraints column = new ColumnConstraints();
                column.setHgrow(Priority.ALWAYS);
                chartPane.getColumnConstraints().add(column);
            }

            while (rows-- > 0) {
                RowConstraints row = new RowConstraints();
                row.setVgrow(Priority.ALWAYS);
                chartPane.getRowConstraints().add(row);
            }
            
            chartPane.getChildren().addAll(charts.getCharts());

            var name = charts.getTitle().isEmpty() ? charts.getCharts().get(0).getTitle() : charts.getTitle();

            if (name == null || name.isEmpty()) {
                name = FXResourceBundle.getBundle().getString​("chart");
            }

            RootPane.get().getActions().setSnapshotContextMenu(chartPane, name);

            BorderPane borderPane = new BorderPane(chartPane);
            if (!charts.getTitle().isEmpty()) {
                var label = new Label(charts.getTitle());
                label.setFont(new Font(label.getFont().getName(), 20));
                borderPane.setTop(label);
                BorderPane.setAlignment(label, Pos.CENTER);
            }

//            ScrollPane scrollPane = new ScrollPane(borderPane);
//            scrollPane.prefViewportWidthProperty().bind(Bindings.createDoubleBinding(
//                    () -> Math.min(borderPane.getWidth(), MainApp.WINDOW_PREF_WIDTH), borderPane.widthProperty()));
//            scrollPane.prefViewportHeightProperty().bind(Bindings.createDoubleBinding(
//                    () -> Math.min(borderPane.getHeight(), MainApp.WINDOW_PREF_HEIGHT), borderPane.heightProperty()));
            // scrollPane.setPannable(true);

            dialog.getDialogPane().setContent(borderPane);

            dialog.show();
        } else if (obj instanceof Tables tables) {
            Dialog<Void> dialog = createPlainDialog(window);

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

            tables.getTableViews().stream().map(tv -> new Tab(tv.getId(), tv))
                    .collect(Collectors.toCollection(() -> tabPane.getTabs()));

            dialog.getDialogPane().setContent(tabPane);
            dialog.show();
        }
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

    private static <T> Dialog<T> createPlainDialog(Window window) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.initModality(Modality.NONE);
        dialog.setDialogPane(new PlainDialogPane());
        dialog.initOwner(window);
        Window dialogWindow = dialog.getDialogPane().getScene().getWindow();
        dialogWindow.setOnCloseRequest(event -> dialogWindow.hide());

        return dialog;
    }

    private static class PlainDialogPane extends DialogPane {

        @Override
        protected Node createButtonBar() {
            return null;
        }

    }
}
