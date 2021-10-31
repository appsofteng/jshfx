package dev.jshfx.base.ui;

import java.nio.file.Path;

import dev.jshfx.jfx.file.FXPath;
import javafx.beans.binding.Bindings;

public class PathPane extends ContentPane {

    private FXPath fxpath;

    public PathPane(Path p) {
        this.fxpath = new FXPath(p);
        
        title.bind(
                Bindings.createStringBinding(() -> createTitle(), fxpath.nameProperty(), modifiedProperty()));
        longTitle.bind(Bindings.createStringBinding(() -> fxpath.getPath().toString(), fxpath.pathProperty()));
    }
    
    private String createTitle() {
        String result = isModified() ? "*" + fxpath.getName() : fxpath.getName();

        return result;
    }
    
    public FXPath getFXPath() {
        return fxpath;
    }
}
