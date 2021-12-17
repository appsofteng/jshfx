package dev.jshfx.access.sys;

import java.nio.file.Path;
import java.util.List;

import dev.jshfx.access.jsh.WindowOptions;
import javafx.scene.Node;

public interface JShService {
    
    Path getCurDir() ;
    void show(List<Node> nodes, WindowOptions options);
}
