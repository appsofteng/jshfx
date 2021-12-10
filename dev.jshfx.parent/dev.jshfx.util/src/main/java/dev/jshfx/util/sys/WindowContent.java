package dev.jshfx.util.sys;

import java.util.List;

import javafx.scene.Node;

public class WindowContent {

    private String title = "";
    private int columns;
    private List<Node> nodes;

    WindowContent(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getTitle() {
        return title;
    }

    public WindowContent setTitle(String title) {
        this.title = title;

        return this;
    }

    public int getColumns() {
        return columns;
    }

    public WindowContent setColumns(int columns) {
        this.columns = columns;

        return this;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
