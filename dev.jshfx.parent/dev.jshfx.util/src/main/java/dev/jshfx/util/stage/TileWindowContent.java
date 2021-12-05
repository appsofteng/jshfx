package dev.jshfx.util.stage;

import java.util.List;

import javafx.scene.Node;

public final class TileWindowContent extends WindowContent {

    private String title;
    private int columns;
    
    TileWindowContent(String title, int columns, List<Node> nodes) {
        super(nodes);
        this.title = title;
        this.columns = columns;
    }
    
    public String getTitle() {
        return title;
    }

    public int getColumns() {
        return columns;
    }
}
