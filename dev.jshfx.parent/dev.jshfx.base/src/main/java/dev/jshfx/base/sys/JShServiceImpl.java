package dev.jshfx.base.sys;

import java.nio.file.Path;
import java.util.List;

import dev.jshfx.base.ui.RootPane;
import dev.jshfx.base.ui.WindowUtils;
import dev.jshfx.util.jsh.WindowOptions;
import dev.jshfx.util.sys.JShService;
import javafx.application.Platform;
import javafx.scene.Node;

public class JShServiceImpl implements JShService {

    @Override
    public void show(List<Node> nodes, WindowOptions options) {
        Platform.runLater(() -> WindowUtils.show(RootPane.get().getScene().getWindow(), nodes, options));
    }

    @Override
    public Path getCurDir() {
        var path = RootPane.get().getContentPane().getFXPath().getPath();

        if (path.isAbsolute()) {
            path = path.getParent();
        } else {
            path = null;
        }

        return path;
    }
}
