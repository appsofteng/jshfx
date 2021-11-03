package dev.jshfx.base.ui;

import dev.jshfx.base.MainApp;
import dev.jshfx.jfx.scene.chart.ChartUtils;
import dev.jshfx.util.chart.Charts;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Window;

public final class DialogUtils {

    private DialogUtils() {
    }

    public static void show(Window window, Object obj) {

        if (obj instanceof Charts charts) {
            ObservableList<Chart> chartsFX = ChartUtils.convert(charts);

            Dialog<Void> dialog = new Dialog<>();
            dialog.initOwner(window);
            TilePane tilePane = new TilePane();
            tilePane.setPrefColumns(charts.getColumns());
            tilePane.getChildren().addAll(chartsFX);

            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.prefViewportWidthProperty().bind(Bindings.createDoubleBinding(() -> Math.min(tilePane.getWidth(), MainApp.WINDOW_PREF_WIDTH) , tilePane.widthProperty()));
            scrollPane.setPrefHeight(MainApp.WINDOW_PREF_HEIGHT);
            scrollPane.setPannable(true);
            
            dialog.getDialogPane().setContent(scrollPane);
            Window dialogWindow = dialog.getDialogPane().getScene().getWindow();
            dialogWindow.setOnCloseRequest(event -> dialogWindow.hide());
            dialog.showAndWait();
        }
    }
}
