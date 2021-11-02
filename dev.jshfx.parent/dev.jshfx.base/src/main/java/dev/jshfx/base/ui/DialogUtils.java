package dev.jshfx.base.ui;

import dev.jshfx.jfx.scene.chart.ChartUtils;
import dev.jshfx.util.chart.ChartList;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;

public final class DialogUtils {

    private DialogUtils() {
    }

    public static void show(Object obj) {

        if (obj instanceof ChartList chartList) {
            ObservableList<Chart> charts = ChartUtils.create(chartList);
        }
    }
}
