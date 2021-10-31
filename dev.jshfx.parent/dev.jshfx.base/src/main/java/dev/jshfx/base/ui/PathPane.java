package dev.jshfx.base.ui;

import java.nio.file.Path;

import dev.jshfx.jfx.file.FXPath;
import javafx.beans.binding.Bindings;

public class PathPane extends ContentPane {

    private FXPath path;

    public PathPane(Path p) {
        this.path = new FXPath(p);
        
        title.bind(
                Bindings.createStringBinding(() -> createTitle(), path.nameProperty(), modifiedProperty()));
        longTitle.bind(Bindings.createStringBinding(() -> path.getPath().toString(), path.pathProperty()));
    }
    
    private String createTitle() {
        String result = isModified() ? "*" + path.getName() : path.getName();

        return result;
    }
    
    public FXPath getPath() {
        return path;
    }
}
