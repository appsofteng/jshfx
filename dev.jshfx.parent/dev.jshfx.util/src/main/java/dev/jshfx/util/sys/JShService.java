package dev.jshfx.util.sys;

import java.nio.file.Path;
import java.util.List;

import dev.jshfx.util.jsh.WindowOptions;
import javafx.scene.Node;

public interface JShService {
    
    Path getCurDir() ;
    void show(List<Node> nodes, WindowOptions options);
}
