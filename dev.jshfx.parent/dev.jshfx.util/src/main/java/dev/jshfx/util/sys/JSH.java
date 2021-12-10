package dev.jshfx.util.sys;

import java.util.Arrays;
import java.util.List;

import javafx.scene.Node;

public sealed class JSH permits $ {

    JSH() {
    }
    
    public static WindowContent show(Node... nodes) {
        return show(Arrays.asList(nodes));
    }

    public static WindowContent show(List<Node> nodes) {
        
        if (nodes == null || nodes.isEmpty() || nodes.contains(null)) {
            return null;
        }
        
        WindowContent content = new WindowContent(nodes);

        return content;
    }
}
