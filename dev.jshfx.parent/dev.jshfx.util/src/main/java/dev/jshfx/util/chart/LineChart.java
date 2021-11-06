package dev.jshfx.util.chart;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Line Chart plots a line connecting the data points in a series. 
 * The data points themselves can be represented by symbols optionally. Line charts are usually used to view data trends over time or category.
 */
public class LineChart<X,Y> extends XYChart<X,Y> {

    public LineChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }
    
    public LineChart(Stream<? extends Number> x, Stream<? extends Number> y) {
        super(x, y);
    }
    
    public LineChart(Stream<? extends Number> x, Function<? extends Number, ? extends Number> y) {
        super(x, y);
    }
}
