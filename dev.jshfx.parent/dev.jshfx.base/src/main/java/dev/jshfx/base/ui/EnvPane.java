package dev.jshfx.base.ui;

import java.util.HashMap;
import java.util.Map;

import org.controlsfx.control.action.Action;

import javafx.scene.layout.StackPane;

public class EnvPane extends StackPane {

    protected Map<Action, Runnable> handlers = new HashMap<>();
    
    
    public void setActions(Actions actions) {
        
    }
    
    public void bindActions(Actions actions) {
        
    }
    
    public void handle(Action action) {
        var handler = handlers.get(action);
        
        if (handler != null) {
            handler.run();
        }
    }
}
