package dev.jshfx.base.sys;

import java.nio.file.Path;
import java.util.List;

import dev.jshfx.access.jsh.WindowOptions;
import dev.jshfx.access.sys.JShService;
import dev.jshfx.base.ui.RootPane;
import dev.jshfx.base.ui.WindowUtils;
import javafx.application.Platform;
import javafx.scene.Node;

public class JShServiceImpl implements JShService {

    @Override
    public void show(List<Node> nodes, WindowOptions options) {
        Platform.runLater(() -> WindowUtils.show(RootPane.get().getScene().getWindow(), nodes, options));
    }

    @Override
    public Path resolve(String path) {
        return RootPane.get().getContentPane().resolve(path);
    }
}
