package dev.jshfx.base.sys;

import java.nio.file.Path;

import dev.jshfx.base.ui.RootPane;
import dev.jshfx.base.ui.WindowUtils;
import dev.jshfx.util.jsh.WindowContent;
import dev.jshfx.util.sys.JShService;
import javafx.application.Platform;

public class JShServiceImpl implements JShService {

    @Override
    public void show(WindowContent content) {
        Platform.runLater(() -> WindowUtils.show(RootPane.get().getScene().getWindow(), content));
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
