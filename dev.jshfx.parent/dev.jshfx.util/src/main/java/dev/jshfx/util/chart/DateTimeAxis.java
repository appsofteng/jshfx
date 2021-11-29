package dev.jshfx.util.chart;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import javafx.scene.chart.Axis;

public class DateTimeAxis extends Axis<ZonedDateTime> {

    private double dataMinValue;
    private double dataMaxValue;
    private TimeAxis timeAxis = new TimeAxis();

    @Override
    protected Object autoRange(double length) {
        double labelSize = getTickLabelFont().getSize() * 2;        
        return timeAxis.autoRange(dataMinValue, dataMaxValue, length, labelSize);
    }

    @Override
    protected void setRange(Object range, boolean animate) {
        timeAxis.setRange(range, animate);
    }

    @Override
    protected Object getRange() {
        return timeAxis.getRange();
    }

    @Override
    public double getZeroPosition() {
        return timeAxis.getZeroPosition();
    }

    @Override
    public double getDisplayPosition(ZonedDateTime value) {
        return timeAxis.getDisplayPosition(value.toInstant().toEpochMilli());
    }

    @Override
    public ZonedDateTime getValueForDisplay(double displayPosition) {        
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeAxis.getValueForDisplay(displayPosition)), ZoneId.systemDefault());
    }

    @Override
    public boolean isValueOnAxis(ZonedDateTime value) {
        return timeAxis.isValueOnAxis(value.toInstant().toEpochMilli());
    }

    @Override
    public double toNumericValue(ZonedDateTime value) {
        return value.toInstant().toEpochMilli();
    }

    @Override
    public ZonedDateTime toRealValue(double value) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) value), ZoneId.systemDefault());
    }

    @Override
    protected List<ZonedDateTime> calculateTickValues(double length, Object range) {
        return timeAxis.calculateTickValues(length, range).stream()
                .map(t -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault())).toList();
    }

    @Override
    protected String getTickMarkLabel(ZonedDateTime value) {
        return timeAxis.getTickMarkLabel(value.toInstant().toEpochMilli());
    }
    
    @Override
    public void invalidateRange(List<ZonedDateTime> data) {
        if (data.isEmpty()) {
            dataMaxValue = 0;
            dataMinValue = 0;
        } else {
            dataMinValue = Double.MAX_VALUE;
            dataMaxValue = -Double.MAX_VALUE;
        }
        for(ZonedDateTime dataValue: data) {
            dataMinValue = Math.min(dataMinValue, dataValue.toInstant().toEpochMilli());
            dataMaxValue = Math.max(dataMaxValue, dataValue.toInstant().toEpochMilli());
        }
        super.invalidateRange(data);
    }
}
