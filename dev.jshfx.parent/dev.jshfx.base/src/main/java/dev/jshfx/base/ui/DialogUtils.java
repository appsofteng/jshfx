package dev.jshfx.base.ui;

import java.util.TreeSet;
import java.util.function.Consumer;

import org.controlsfx.dialog.ProgressDialog;

import dev.jshfx.base.MainApp;
import dev.jshfx.jfx.scene.control.AutoCompleteField;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.util.chart.Charts;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.stage.Window;

public final class DialogUtils {

    private DialogUtils() {
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
            dialog.setDialogPane(new PlainDialogPane());
            dialog.initOwner(window);

            TilePane tilePane = new TilePane();
            tilePane.setPrefColumns(charts.getColumns());
            tilePane.getChildren().addAll(charts.getCharts());
            
            var name = charts.getTitle().isEmpty() ? charts.getCharts().get(0).getTitle() : charts.getTitle();
            
            if (name == null || name.isEmpty()) {
                name = FXResourceBundle.getBundle().getString​("chart");
            }
            
            RootPane.get().getActions().setSnapshotContextMenu(tilePane, name);

            BorderPane borderPane = new BorderPane(tilePane);
            if (!charts.getTitle().isEmpty()) {
                var label = new Label(charts.getTitle());
                label.setFont(new Font(label.getFont().getName(), 20));
                borderPane.setTop(label);
                BorderPane.setAlignment(label, Pos.CENTER);
            }

            ScrollPane scrollPane = new ScrollPane(borderPane);
            scrollPane.prefViewportWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(borderPane.getWidth(), MainApp.WINDOW_PREF_WIDTH), borderPane.widthProperty()));
            scrollPane.prefViewportHeightProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(borderPane.getHeight(), MainApp.WINDOW_PREF_HEIGHT), borderPane.heightProperty()));
            scrollPane.setPannable(true);

            dialog.getDialogPane().setContent(scrollPane);

            dialog.show();
        }
    }

    public static void showHistorySearch(Window window, ObservableList<String> history, Consumer<String> onSelection) {
        Tooltip popup = new Tooltip();
        popup.setAutoHide(true);
        AutoCompleteField<String> autoCompleteField = new AutoCompleteField<>(new TreeSet<>(history));
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
