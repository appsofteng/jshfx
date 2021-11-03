package dev.jshfx.util.chart;

import java.util.Arrays;
import java.util.List;

public final class Charts {

    private int columns;
    
    private List<Chart> charts;
    
    private Charts (int columns, Chart... charts) {
        this.charts = Arrays.asList(charts);
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
