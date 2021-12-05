package dev.jshfx.util.stage;

import java.util.Arrays;
import java.util.List;

import javafx.scene.Node;

public final class Windows {

    private Windows() {
    }

    public static WindowContent show(Node... nodes) {
        return show("", nodes);
    }

    public static WindowContent show(int columns, Node... nodes) {
        return show("", columns, nodes);
    }

    public static WindowContent show(String title, Node... nodes) {
        return show(title, Arrays.asList(nodes));
    }

    public static WindowContent show(String title, int columns, Node... nodes) {
        return show(title, columns, Arrays.asList(nodes));
    }

    public static WindowContent show(List<Node> nodes) {
        return show("", nodes);
    }

    public static WindowContent show(int columns, List<Node> nodes) {

        return show("", columns, nodes);
    }

    public static WindowContent show(String title, List<Node> nodes) {
        var columns = nodes.size() > 1 ? 2 : 1;
        return show(title, columns, nodes);
    }

    public static WindowContent show(String title, int columns, List<Node> nodes) {

        TileWindowContent content = new TileWindowContent(title, columns, nodes);

        return content;
    }
    
    public static WindowContent showTabs(Node... nodes) {
        return showTabs(Arrays.asList(nodes));
    }
    
    public static WindowContent showTabs(List<Node> nodes) {

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId() == null) {
                nodes.get(i).setId("T" + i);
            }
        }

        WindowContent content = new WindowContent(nodes);

        return content;
    }
}
