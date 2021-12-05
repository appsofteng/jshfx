package dev.jshfx.util.stage;

import java.util.List;

import javafx.scene.Node;

public class WindowContent {

    private List<Node> nodes;
        
    WindowContent(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }    
}
