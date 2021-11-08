package dev.jshfx.base.ui;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.controlsfx.dialog.ProgressDialog;

import dev.jshfx.base.MainApp;
import dev.jshfx.j.beans.BeanConverter;
import dev.jshfx.jfx.scene.control.AutoCompleteField;
import dev.jshfx.util.chart.Charts;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.TilePane;
import javafx.stage.Window;

public final class DialogUtils {

    
    private static final BeanConverter beanConverter = new BeanConverter(Map.of("dev.jshfx.util.chart", "javafx.scene.chart", "dev.jshfx.util.geometry", "javafx.geometry"));
    
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
            
            List<Chart> chartsFX = charts.getCharts().stream().map(c -> (Chart) beanConverter.convert(c)).collect(Collectors.toList());            

            Dialog<Void> dialog = createPlainDialog(window);
            dialog.setDialogPane(new PlainDialogPane());
            dialog.initOwner(window);
            TilePane tilePane = new TilePane();
            tilePane.setPrefColumns(charts.getColumns());
            tilePane.getChildren().addAll(chartsFX);

            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.prefViewportWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(tilePane.getWidth(), MainApp.WINDOW_PREF_WIDTH), tilePane.widthProperty()));
            scrollPane.prefViewportHeightProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(tilePane.getHeight(), MainApp.WINDOW_PREF_HEIGHT), tilePane.heightProperty()));
            scrollPane.setPannable(true);

            dialog.getDialogPane().setContent(scrollPane);

            dialog.show();
        } else  if (obj instanceof LineChart chart) {
            

            Dialog<Void> dialog = createPlainDialog(window);
            dialog.setDialogPane(new PlainDialogPane());
            dialog.initOwner(window);
            TilePane tilePane = new TilePane();
            tilePane.setPrefColumns(1);
            tilePane.getChildren().addAll(chart);

            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.prefViewportWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(tilePane.getWidth(), MainApp.WINDOW_PREF_WIDTH), tilePane.widthProperty()));
            scrollPane.prefViewportHeightProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(tilePane.getHeight(), MainApp.WINDOW_PREF_HEIGHT), tilePane.heightProperty()));
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
