package dev.jshfx.util.chart;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.chart.XYChart.Data;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public final class Charts {

    private String title;
    private int columns;

    private List<Chart> charts;

    private Charts(String title, int columns, List<Chart> charts) {
        this.title = title;
        this.columns = columns;
        this.charts = charts;
    }

    public static <X, Y> AreaChart<X, Y> getAreaChart(Series<X, Y>... series) {
        return getAreaChart(Arrays.asList(series));
    }

    public static <X, Y> AreaChart<X, Y> getAreaChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        AreaChart<X, Y> chart = (AreaChart<X, Y>) new AreaChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }

    public static <X, Y> BarChart<X, Y> getBarChart(Series<X, Y>... series) {
        return getBarChart(Arrays.asList(series));
    }

    public static <X, Y> BarChart<X, Y> getBarChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        BarChart<X, Y> chart = (BarChart<X, Y>) new BarChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }
    
    public static <X, Y> BubbleChart<X, Y> getBubbleChart(Series<X, Y>... series) {
        return getBubbleChart(Arrays.asList(series));
    }
    
    public static <X, Y> BubbleChart<X, Y> getBubbleChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        BubbleChart<X, Y> chart = (BubbleChart<X, Y>) new BubbleChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }

    public static <X, Y> LineChart<X, Y> getLineChart(Series<X, Y>... series) {
        return getLineChart(Arrays.asList(series));
    }

    public static <X, Y> LineChart<X, Y> getLineChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        LineChart<X, Y> chart = (LineChart<X, Y>) new LineChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }

    public static <Y extends Number> PieChart getPieChart(List<PieChart.Data> data) {
        PieChart chart = new PieChart();
        chart.getData().addAll(data);

        return chart;
    }

    public static <X, Y> ScatterChart<X, Y> getScatterChart(Series<X, Y>... series) {
        return getScatterChart(Arrays.asList(series));
    }

    public static <X, Y> ScatterChart<X, Y> getScatterChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        ScatterChart<X, Y> chart = (ScatterChart<X, Y>) new ScatterChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }

    public static <X, Y> StackedAreaChart<X, Y> getStackedAreaChart(Series<X, Y>... series) {
        return getStackedAreaChart(Arrays.asList(series));
    }

    public static <X, Y> StackedAreaChart<X, Y> getStackedAreaChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        StackedAreaChart<X, Y> chart = (StackedAreaChart<X, Y>) new StackedAreaChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }

    public static <X, Y> StackedBarChart<X, Y> getStackedBarChart(Series<X, Y>... series) {
        return getStackedBarChart(Arrays.asList(series));
    }

    public static <X, Y> StackedBarChart<X, Y> getStackedBarChart(List<Series<X, Y>> series) {
        List<Axis<?>> axes = getAxes(series);

        StackedBarChart<X, Y> chart = (StackedBarChart<X, Y>) new StackedBarChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }

    private static <X, Y> List<Axis<?>> getAxes(List<Series<X, Y>> series) {

        X x = series.get(0).getData().get(0).getXValue();
        Y y = series.get(0).getData().get(0).getYValue();

        Axis<X> axisX = getAxis(x);
        Axis<Y> axisY = getAxis(y);

        if (series.size() > 1) {
            sortCategories(axisX, axisY, series);
        }

        if (axisX instanceof NumberAxis numberAxis) {
            series.stream().map(s -> s.getData().get(0)).mapToDouble(d ->  ((Number)d.getXValue()).doubleValue()).min()
                    .ifPresent(m -> numberAxis.setForceZeroInRange(m <= 0));
        }

        return List.of(axisX, axisY);
    }

    private static <V> Axis<V> getAxis(V value) {
        Axis<V> axis = null;

        if (value instanceof Number) {
            axis = (Axis<V>) new NumberAxis();
        } else {
            axis = (Axis<V>) new CategoryAxis();
        }

        return axis;
    }

    private static <X, Y> void sortCategories(Axis<X> axisX, Axis<Y> axisY, List<Series<X, Y>> series) {

        CategoryAxis categoryX = null;
        CategoryAxis categoryY = null;

        Set<String> setX = null;
        Set<String> setY = null;

        if (axisX instanceof CategoryAxis categoryAxis) {
            categoryX = categoryAxis;
            setX = new TreeSet<>();
        }

        if (axisY instanceof CategoryAxis categoryAxis) {
            categoryY = categoryAxis;
            setY = new TreeSet<>();
        }

        for (var s : series) {
            for (var d : s.getData()) {
                if (setX != null) {
                    setX.add(d.getXValue().toString());
                }

                if (setY != null) {
                    setY.add(d.getYValue().toString());
                }
            }
        }

        if (categoryX != null) {
            categoryX.setCategories(FXCollections.observableArrayList(setX));
            categoryX.setAutoRanging(true);
        }

        if (categoryY != null) {
            categoryY.setCategories(FXCollections.observableArrayList(setY));
            categoryY.setAutoRanging(true);
        }
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
        return show("", charts);
    }

    public static Charts show(int columns, Chart... charts) {
        return show("", columns, charts);
    }

    public static Charts show(String title, Chart... charts) {
        return show(title, Arrays.asList(charts));
    }

    public static Charts show(String title, int columns, Chart... charts) {
        return show(title, columns, Arrays.asList(charts));
    }

    public static Charts show(List<Chart> charts) {
        return show("", charts);
    }

    public static Charts show(int columns, List<Chart> charts) {

        return show("", columns, charts);
    }

    public static Charts show(String title, List<Chart> charts) {
        var columns = charts.size() > 1 ? 2 : 1;
        return show(title, columns, charts);
    }

    public static Charts show(String title, int columns, List<Chart> charts) {

        Charts result = new Charts(title, columns, charts);

        return result;
    }
}
