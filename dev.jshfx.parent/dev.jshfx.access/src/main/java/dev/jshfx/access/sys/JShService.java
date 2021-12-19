package dev.jshfx.access.sys;

import java.nio.file.Path;
import java.util.List;

import dev.jshfx.access.jsh.WindowOptions;
import javafx.scene.Node;

public interface JShService {
    
    Path resolve(String path) ;
    void show(List<Node> nodes, WindowOptions options);
}
