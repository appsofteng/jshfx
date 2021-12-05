package dev.jshfx.util.stage;

import java.util.Arrays;
import java.util.List;

import javafx.scene.Node;

public final class Windows {

    private Windows() {
    }

    public static WindowContent show(Node... nodes) {
        return show(Arrays.asList(nodes));
    }

    public static WindowContent show(List<Node> nodes) {
        WindowContent content = new WindowContent(nodes);

        return content;
    }
}
