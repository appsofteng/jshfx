package dev.jshfx.util.chart;

import java.util.ArrayList;
import java.util.List;

public class Series<X,Y> {

    private List<X> xValues = new ArrayList<>();
    private List<Y> yValues = new ArrayList<>();
    
    public List<X> getXValues() {
        return xValues;
    }
    
    public List<Y> getYValues() {
        return yValues;
    }
    
    public int size() {
        return Math.min(xValues.size(), yValues.size());
    }
}
