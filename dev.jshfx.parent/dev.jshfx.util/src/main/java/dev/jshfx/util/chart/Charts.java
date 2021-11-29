package dev.jshfx.util.chart;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
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

    
    public static <X, Y> AreaChart<X, Y> getAreaChart(Series<X, Y>... series) {
        List<Axis<?>> axes = getAxes(series[0]);

        AreaChart<X, Y> chart = (AreaChart<X, Y>) new AreaChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }
    
    public static <X, Y> BarChart<X, Y> getBarChart(Series<X, Y>... series) {
        List<Axis<?>> axes = getAxes(series[0]);

        BarChart<X, Y> chart = (BarChart<X, Y>) new BarChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }
    
    public static <X, Y> LineChart<X, Y> getLineChart(Series<X, Y>... series) {
        List<Axis<?>> axes = getAxes(series[0]);

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
        List<Axis<?>> axes = getAxes(series[0]);

        ScatterChart<X, Y> chart = (ScatterChart<X, Y>) new ScatterChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }
    
    public static <X, Y> StackedAreaChart<X, Y> getStackedAreaChart(Series<X, Y>... series) {
        List<Axis<?>> axes = getAxes(series[0]);

        StackedAreaChart<X, Y> chart = (StackedAreaChart<X, Y>) new StackedAreaChart<>(axes.get(0), axes.get(1));
        chart.getData().addAll(series);

        return chart;
    }
    
    public static <X, Y> StackedBarChart<X, Y> getStackedBarChart(Series<X, Y>... series) {
        List<Axis<?>> axes = getAxes(series[0]);

        StackedBarChart<X, Y> chart = (StackedBarChart<X, Y>) new StackedBarChart<>(axes.get(0), axes.get(1));        
        chart.getData().addAll(series);

        return chart;
    }
    
    private static <X,Y> List<Axis<?>> getAxes(Series<X,Y> series) {
        X x = series.getData().get(0).getXValue();
        Y y = series.getData().get(0).getYValue();
        
        Axis<X> axisX = getAxis(x);
        Axis<Y> axisY = getAxis(y);
        
        return List.of(axisX, axisY);
    }
    
    private static <V> Axis<V> getAxis(V value) {
        Axis<V> axis = null;
        
        if (value instanceof Number) {
            axis = (Axis<V>) new NumberAxis();
        } else if (value instanceof ZonedDateTime) {
            axis = (Axis<V>) new DateTimeAxis();
        } else {
            axis = (Axis<V>) new CategoryAxis();
        }
        
        return axis;
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
