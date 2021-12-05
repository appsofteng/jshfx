package dev.jshfx.util.stage;

import java.util.List;

import javafx.scene.Node;

public class WindowContent {

    private String title;
    private int columns;
    private List<Node> nodes;
        
    WindowContent(String title, int columns, List<Node> nodes) {
        this.nodes = nodes;
        this.title = title;
        this.columns = columns;
    }

    public String getTitle() {
        return title;
    }

    public int getColumns() {
        return columns;
    }
    
    public List<Node> getNodes() {
        return nodes;
    }    
}
