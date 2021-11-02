package dev.jshfx.jfx.scene.chart;

import java.util.ArrayList;
import java.util.List;

import dev.jshfx.util.chart.Axis;
import dev.jshfx.util.chart.ChartList;
import dev.jshfx.util.chart.LineChart;
import dev.jshfx.util.chart.Series;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ChartUtils {

    private ChartUtils() {
    }

    public static ObservableList<javafx.scene.chart.Chart> create(ChartList chartList) {
        ObservableList<javafx.scene.chart.Chart> list = FXCollections.observableArrayList();

        return list;
    }

    private static <X, Y> javafx.scene.chart.LineChart<X, Y> create(LineChart<X, Y> chart) {
        javafx.scene.chart.LineChart<X, Y> lineChart = new javafx.scene.chart.LineChart<>(create(chart.getXAxis()),
                create(chart.getYAxis()));
        lineChart.getData().addAll(create(chart.getSeries()));

        return lineChart;
    }

    private static <T> javafx.scene.chart.Axis<T> create(Axis<T> axis) {
        var a = new javafx.scene.chart.NumberAxis();

        return (javafx.scene.chart.Axis<T>) a;
    }

    private static <X, Y> List<javafx.scene.chart.XYChart.Series<X, Y>> create(List<Series<X, Y>> seriesList) {

        List<javafx.scene.chart.XYChart.Series<X, Y>> dataSeriesList = new ArrayList<>();

        for (Series<X, Y> series : seriesList) {
            javafx.scene.chart.XYChart.Series<X, Y> dataSeries = new javafx.scene.chart.XYChart.Series<>();

            for (int i = 0; i < series.size(); i++) {
                dataSeries.getData().add(
                        new javafx.scene.chart.XYChart.Data<>(series.getXValues().get(i), series.getYValues().get(i)));
            }

            dataSeriesList.add(dataSeries);
        }

        return dataSeriesList;
    }
}
