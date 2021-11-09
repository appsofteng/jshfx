package dev.jshfx.util.chart;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;

public final class Charts {

    private String title;
    private int columns;

    private List<Chart> charts;

    private Charts(String title, int columns, Chart... charts) {
        this.title = title;
        this.columns = columns;
        this.charts = Arrays.asList(charts);
    }

    public static <X extends Number, Y extends Number> LineChart<X, Y> getLineChart(Stream<X> x, Stream<Y> y) {
        LineChart<X, Y> lineChart = (LineChart<X, Y>) new LineChart<Number, Number>(new NumberAxis(), new NumberAxis());

        var series = getSeries(x, y);
        lineChart.getData().add(series);

        return lineChart;
    }

    public static <X extends Number, Y extends Number> LineChart<X, Y> getLineChart(Stream<X> x, Function<X, Y> y) {
        LineChart<X, Y> chart = (LineChart<X, Y>) new LineChart<Number, Number>(new NumberAxis(), new NumberAxis());

        var series = getSeries(x, y);
        chart.getData().add(series);

        return chart;
    }

    public static <Y extends Number> BarChart<String, Y> getCategoryBarChart(Map<String, Y> map) {
        BarChart<String, Y> chart = (BarChart<String, Y>) new BarChart<String, Number>(new CategoryAxis(),
                new NumberAxis());
        var series = getSeries(map);
        chart.getData().add(series);

        return chart;
    }

    public static <X extends Number, Y extends Number> BarChart<X, Y> getBarChart(Map<X, Y> map) {
        BarChart<X, Y> chart = (BarChart<X, Y>) new BarChart<Number, Number>(new NumberAxis(), new NumberAxis());
        var series = getSeries(map);
        chart.getData().add(series);

        return chart;
    }

    public static <X, Y> Series<X, Y> getSeries(Stream<X> xs, Stream<Y> ys) {
        Series<X, Y> series = new Series<>();
        var iterator = ys.iterator();
        Function<X, Y> yf = x -> iterator.hasNext() ? iterator.next() : null;

        xs.map(x -> new Data<>(x, yf.apply(x))).takeWhile(e -> e.getYValue() != null)
                .collect(Collectors.toCollection(() -> series.getData()));

        return series;
    }

    public static <X, Y> Series<X, Y> getSeries(Stream<X> xs, Function<X, Y> yf) {
        Series<X, Y> series = new Series<>();

        xs.map(x -> new Data<>(x, yf.apply(x))).takeWhile(e -> e.getYValue() != null)
                .collect(Collectors.toCollection(() -> series.getData()));

        return series;
    }

    public static <X, Y> Series<X, Y> getSeries(Map<X, Y> map) {
        Series<X, Y> series = new Series<>();

        map.entrySet().stream().map(e -> new Data<>(e.getKey(), e.getValue()))
                .collect(Collectors.toCollection(() -> series.getData()));

        return series;
    }

    public String getTitle() {
        return title;
    }

    public int getColumns() {
        return columns;
    }

    public List<Chart> getCharts() {
        return charts;
    }

    public static Charts show(Chart... charts) {
        var columns = charts.length > 1 ? 2 : 1;
        return show(columns, charts);
    }

    public static Charts show(int columns, Chart... charts) {

        return show("", columns, charts);
    }

    public static Charts show(String title, Chart... charts) {
        var columns = charts.length > 1 ? 2 : 1;
        return show(title, columns, charts);
    }

    public static Charts show(String title, int columns, Chart... charts) {

        Charts result = new Charts(title, columns, charts);

        return result;
    }
}
