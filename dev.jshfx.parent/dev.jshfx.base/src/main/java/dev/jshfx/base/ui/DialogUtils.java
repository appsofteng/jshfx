package dev.jshfx.base.ui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jshfx.base.MainApp;
import dev.jshfx.j.beans.BeanConverter;
import dev.jshfx.util.chart.Charts;
import javafx.beans.binding.Bindings;
import javafx.scene.chart.Chart;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Window;

public final class DialogUtils {

    
    private static final BeanConverter beanConverter = new BeanConverter(Map.of("dev.jshfx.util.chart", "javafx.scene.chart", "dev.jshfx.util.geometry", "javafx.geometry"));
    
    private DialogUtils() {
    }

    public static void show(Window window, Object obj) {

        if (obj instanceof Charts charts) {
            
            List<Chart> chartsFX = charts.getCharts().stream().map(c -> (Chart) beanConverter.convert(c)).collect(Collectors.toList());            

            Dialog<Void> dialog = new Dialog<>();
            dialog.initOwner(window);
            TilePane tilePane = new TilePane();
            tilePane.setPrefColumns(charts.getColumns());
            tilePane.getChildren().addAll(chartsFX);

            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.prefViewportWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(tilePane.getWidth(), MainApp.WINDOW_PREF_WIDTH), tilePane.widthProperty()));
            scrollPane.setPrefHeight(MainApp.WINDOW_PREF_HEIGHT);
            scrollPane.setPannable(true);

            dialog.getDialogPane().setContent(scrollPane);
            Window dialogWindow = dialog.getDialogPane().getScene().getWindow();
            dialogWindow.setOnCloseRequest(event -> dialogWindow.hide());
            dialog.showAndWait();
        }
    }
}
