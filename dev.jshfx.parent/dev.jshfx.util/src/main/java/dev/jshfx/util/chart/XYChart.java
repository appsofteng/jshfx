package dev.jshfx.util.chart;

import java.util.ArrayList;
import java.util.List;

public class XYChart<X,Y> extends Chart {

    private Axis<X> xAxis;
    private Axis<Y> yAxis;
    private List<Series<X,Y>> series = new ArrayList<>();
    
    public XYChart(Axis<X> xAxis, Axis<Y> yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
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
