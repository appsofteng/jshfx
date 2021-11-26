package dev.jshfx.util.chart;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public final class Data {

    private Data() {
    }
    
    public static <X, Y> Series<X, Y> getSeries(Stream<X> xs, Stream<Y> ys) {
        Series<X, Y> series = new Series<>();
        var iterator = ys.iterator();
        Function<X, Y> yf = x -> iterator.hasNext() ? iterator.next() : null;

        xs.map(x -> new XYChart.Data<>(x, yf.apply(x))).takeWhile(e -> e.getYValue() != null)
                .collect(Collectors.toCollection(() -> series.getData()));

        return series;
    }

    public static <X, Y> Series<X, Y> getSeries(Stream<X> xs, Function<X, Y> yf) {
        Series<X, Y> series = new Series<>();

        xs.map(x -> new XYChart.Data<>(x, yf.apply(x))).takeWhile(e -> e.getYValue() != null)
                .collect(Collectors.toCollection(() -> series.getData()));

        return series;
    }

    public static <X, Y> Series<X, Y> getSeries(Map<X, Y> data) {
        Series<X, Y> series = new Series<>();

        data.entrySet().stream().map(e -> new XYChart.Data<>(e.getKey(), e.getValue()))
                .collect(Collectors.toCollection(() -> series.getData()));

        return series;
    }
    
    public static <Y extends Number> List<PieChart.Data> getPieData(Map<String, Y> data) {
        List<PieChart.Data> list = data.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue().doubleValue())).toList();

        return list;
    }
}
