package dev.jshfx.access.jsh;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import dev.jshfx.access.sys.JShService;
import javafx.scene.Node;

public sealed class JSh permits $ {

    private static JShService jshService;

    static {
        ServiceLoader<JShService> loader = ServiceLoader.load(JShService.class);
        jshService = loader.findFirst().get();
    }

    JSh() {
    }

    public static Path resolve(String path) {
        return jshService.resolve(path);
    }
    
    public static void show(Node... nodes) {
        show(Arrays.asList(nodes));
    }

    public static void show(List<Node> nodes) {
        show(nodes, new WindowOptions());
    }
    
    public static void show(List<Node> nodes, WindowOptions options) {
        if (nodes != null && !nodes.isEmpty() && !nodes.contains(null)) {
            jshService.show(nodes, options);
        }
    }
}
