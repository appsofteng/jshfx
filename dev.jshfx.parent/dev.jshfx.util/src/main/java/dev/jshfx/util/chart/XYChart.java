package dev.jshfx.util.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class XYChart<X,Y> extends Chart {

    private Axis<X> xAxis;
    private Axis<Y> yAxis;
    private List<Series<X,Y>> series = new ArrayList<>();
    
    public XYChart(Axis<X> xAxis, Axis<Y> yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }
    
    @SuppressWarnings("unchecked")
    public XYChart(Stream<? extends Number> x, Stream<? extends Number> y) {
        xAxis = (Axis<X>) new NumberAxis();
        yAxis = (Axis<Y>) new NumberAxis();
        
        Series<? extends Number,? extends Number> s = new Series<>(x,y);
        series.add((Series<X, Y>) s);
    }
    
    @SuppressWarnings("unchecked")
    public XYChart(Stream<? extends Number> x, Function<? extends Number, ? extends Number> y) {
        xAxis = (Axis<X>) new NumberAxis();
        yAxis = (Axis<Y>) new NumberAxis();
        
        Series<Number,Number> s = new Series<>();
        s.setX((Stream<Number>) x);
        s.setY((Function<Number, Number>) y);
        
        series.add((Series<X, Y>) s);
    }
    
    public Axis<X> getXAxis() {
        return xAxis;
    }
    
    public Axis<Y> getYAxis() {
        return yAxis;
    }
    
    public List<Series<X,Y>> getSeries() {
        return series;
    }
}
