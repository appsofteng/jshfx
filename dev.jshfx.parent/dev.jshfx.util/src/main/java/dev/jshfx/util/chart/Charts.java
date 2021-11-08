package dev.jshfx.util.chart;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;

public final class Charts {

    private int columns;
    
    private List<Chart> charts;
    
    private Charts (int columns, Chart... charts) {
        this.columns = columns;
        this.charts = Arrays.asList(charts);
    }
    
    
    public static <X extends Number, Y extends Number> LineChart<X, Y> lineChart(Stream<X> x, Stream<Y> y) {
        LineChart<X, Y> lineChart = (LineChart<X, Y>) new LineChart<Number,Number>(new NumberAxis(), new NumberAxis());
       
       var series = getSeries(x, y);
       lineChart.getData().add(series);
       
       return lineChart;
    }
    
    public static <X, Y> Series<X, Y> getSeries(Stream<X> xs, Stream<Y> ys) {
        Series<X, Y> series = new Series<>();
        var iterator = ys.iterator();
        Function<X, Y> yf = x -> iterator.hasNext() ? iterator.next() : null;
        
        xs.map(x -> new Data<>(x, yf.apply(x))).takeWhile(e -> e.getYValue() != null).collect(Collectors.toCollection(() -> series.getData()));
        
        return series;
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
        
        Charts result = new Charts(columns, charts);
        
        return result;
    }
}
