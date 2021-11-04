package dev.jshfx.util.chart;

import dev.jshfx.util.geometry.Side;

public abstract class Chart {

    private String title;
    private Side titleSide = Side.TOP;
    private boolean legendVisible;
    
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
    
    public boolean isLegendVisible() {
        return legendVisible;
    }
    
    public void setLegendVisible(boolean legendVisible) {
        this.legendVisible = legendVisible;
    }
}
