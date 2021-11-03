package dev.jshfx.util.chart;

import java.util.Arrays;
import java.util.List;

public final class Charts {

    private List<Chart> charts;
    
    private Charts (Chart... charts) {
        this.charts = Arrays.asList(charts);
    }
    
    public List<Chart> getCharts() {
        return charts;
    }
    
    public static Charts show(Chart... charts) {
        
        Charts result = new Charts(charts);
        
        return result;
    }
}
