package dev.jshfx.util.chart;

public class Chart {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        
        private ChartList chartList = new ChartList();
        
        private Builder() {
        }

        public ChartList plot() {
            return chartList;
        }
    }

    public enum ChartType {
        LINE_CHART;
    }
}
