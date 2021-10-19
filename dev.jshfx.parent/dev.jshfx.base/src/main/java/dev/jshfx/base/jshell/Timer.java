package dev.jshfx.base.jshell;

import java.time.Duration;

import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Timer {

    private long start;
    private long end;
    private StringProperty text = new SimpleStringProperty();
    
    public void start() {
        start = System.currentTimeMillis();
        text.set(FXResourceBundle.getBundle().getString​("started"));
    }
    
    public void stop() {
        end = System.currentTimeMillis();
        long period = end - start;
        Duration duration = Duration.ofMillis(period);      
        
        text.set(FXResourceBundle.getBundle().getString​("terminated", duration.toString()));
    }
    
    public ReadOnlyStringProperty textProperty() {
        return text;
    }
}
