package dev.jshfx.jfx.scene;

import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;

public final class NodeUtils {

    private NodeUtils() {
    }
    
    public static void saveSnapshot(Node node, Path file) {
        var image = node.snapshot(null, null);    
        
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
