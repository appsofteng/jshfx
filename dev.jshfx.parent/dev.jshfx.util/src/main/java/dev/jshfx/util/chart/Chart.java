package dev.jshfx.util.chart;

import dev.jshfx.util.geometry.Side;

/**
 * Base class for all charts. It has 3 parts the title, legend and chartContent. The chart content is populated by the specific subclass of Chart.
 *
 */
public abstract class Chart {

    private Side legendSide = Side.BOTTOM;
    private boolean legendVisible;
    private String title;
    private Side titleSide = Side.TOP;
    
    public Side getLegendSide() {
        return legendSide;
    }
    
    public void setLegendSide(Side legendSide) {
        this.legendSide = legendSide;
    }
    
    public boolean isLegendVisible() {
        return legendVisible;
    }
    
    public void setLegendVisible(boolean legendVisible) {
        this.legendVisible = legendVisible;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Side getTitleSide() {
        return titleSide;
    }
    
    public void setTitleSide(Side titleSide) {
        this.titleSide = titleSide;
    }    
}
