package dev.jshfx.jfx.scene.chart;

import java.util.stream.Collectors;

import dev.jshfx.util.chart.Axis;
import dev.jshfx.util.chart.CategoryAxis;
import dev.jshfx.util.chart.Chart;
import dev.jshfx.util.chart.Charts;
import dev.jshfx.util.chart.LineChart;
import dev.jshfx.util.chart.NumberAxis;
import dev.jshfx.util.chart.Series;
import dev.jshfx.util.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ChartConverter {

    private ChartConverter() {
    }

    public static ObservableList<javafx.scene.chart.Chart> convert(Charts charts) {
        ObservableList<javafx.scene.chart.Chart> chartsFX = FXCollections.observableArrayList();
        javafx.scene.chart.Chart chartFX = null;

        for (Chart chart : charts.getCharts()) {

            if (chart instanceof LineChart<?, ?> lineChart) {

                chartFX = convert(lineChart);
            }
            
            convertChart(chart, chartFX);

            chartsFX.add(chartFX);
        }

        return chartsFX;
    }

    private static <X, Y> javafx.scene.chart.LineChart<X, Y> convert(LineChart<X, Y> chart) {
        javafx.scene.chart.LineChart<X, Y> lineChart = new javafx.scene.chart.LineChart<>(convert(chart.getXAxis()),
                convert(chart.getYAxis()));
       
        convertXYChart(chart, lineChart);
        
        return lineChart;
    }

    private static void convertChart(Chart chart, javafx.scene.chart.Chart chartFX) {
        chartFX.setTitle(chart.getTitle());
    }

    private static <X, Y> void convertXYChart(XYChart<X, Y> chart, javafx.scene.chart.XYChart<X, Y> chartFX) {
        chartFX.getData().addAll(chart.getSeries().stream().map(ChartConverter::convert).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private static <T> javafx.scene.chart.Axis<T> convert(Axis<T> axis) {

        javafx.scene.chart.Axis<T> a = null;

        if (axis instanceof NumberAxis) {
            a = (javafx.scene.chart.Axis<T>) new javafx.scene.chart.NumberAxis();
        } else if (axis instanceof CategoryAxis) {
            a = (javafx.scene.chart.Axis<T>) new javafx.scene.chart.CategoryAxis();
        }

        return a;
    }

    private static <X, Y> javafx.scene.chart.XYChart.Series<X, Y> convert(Series<X, Y> series) {

        javafx.scene.chart.XYChart.Series<X, Y> dataSeries = new javafx.scene.chart.XYChart.Series<>();
        series.iterate((x, y) -> dataSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(x, y)));

        return dataSeries;
    }
}
