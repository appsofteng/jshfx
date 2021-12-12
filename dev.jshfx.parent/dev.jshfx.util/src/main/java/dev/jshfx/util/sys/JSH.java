package dev.jshfx.util.sys;

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

public sealed class JSH permits $ {

    private static final ObjectProperty<WindowContent> windowContent = new SimpleObjectProperty<>();
    
    JSH() {
    }
    
    public static ReadOnlyObjectProperty<WindowContent> windowContentProperty() {
        return windowContent;
    }
    
    public static void show(Node... nodes) {
       show(Arrays.asList(nodes));
    }

    public static void show(List<Node> nodes) {
        
        if (nodes != null && !nodes.isEmpty() && !nodes.contains(null)) {
            WindowContent content = new WindowContent(nodes);            
            windowContent.set(content);
        }
    }
}
