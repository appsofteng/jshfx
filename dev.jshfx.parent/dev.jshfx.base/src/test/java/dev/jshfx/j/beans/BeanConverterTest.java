package dev.jshfx.j.beans;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.jshfx.util.chart.Chart;
import dev.jshfx.util.chart.LineChart;
import dev.jshfx.util.chart.NumberAxis;
import javafx.embed.swing.JFXPanel;

public class BeanConverterTest {

    private static BeanConverter converter;
    
    @BeforeAll
    public static void setup() {
       converter = new BeanConverter(
                (Map.of("dev.jshfx.util.chart", "javafx.scene.chart", "dev.jshfx.util.geometry", "javafx.geometry")));
    }
    
    @BeforeEach
    public void setupEach() {
        JFXPanel fxPanel = new JFXPanel();
    }
    
    @Test
    public void testConvertNumberAxis() {        
        
        var axis = new NumberAxis();

        Object result = converter.convert(axis);

       assertTrue(result instanceof javafx.scene.chart.NumberAxis);
    }

    @Test
    public void testConvertLineChart() {
        Chart chart = new LineChart<>(Stream.of(0, 1, 2), Stream.of(10, 20, 30));
        chart.setTitle("Line Chart");

        javafx.scene.chart.LineChart<?,?> result = converter.convert(chart);

        assertTrue(result.getTitle()== chart.getTitle());
        assertTrue(result.getXAxis() instanceof javafx.scene.chart.NumberAxis);
        assertTrue(result.getYAxis() instanceof javafx.scene.chart.NumberAxis);
        assertEquals(3, result.getData().get(0).getData().size());
    }
}
