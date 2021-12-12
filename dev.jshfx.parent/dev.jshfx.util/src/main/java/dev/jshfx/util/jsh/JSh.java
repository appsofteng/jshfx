package dev.jshfx.util.jsh;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import dev.jshfx.util.sys.JShService;
import javafx.scene.Node;

public sealed class JSh permits $ {

    private static JShService jshService;

    static {
        ServiceLoader<JShService> loader = ServiceLoader.load(JShService.class);
        jshService = loader.findFirst().get();
    }

    JSh() {
    }

    public static Path getCurDir() {
        return jshService.getCurDir();
    }
    
    public static void show(Node... nodes) {
        show(Arrays.asList(nodes));
    }

    public static void show(List<Node> nodes) {
        if (nodes != null && !nodes.isEmpty() && !nodes.contains(null)) {
            WindowContent content = new WindowContent(nodes);
            jshService.show(content);
        }
    }
}
