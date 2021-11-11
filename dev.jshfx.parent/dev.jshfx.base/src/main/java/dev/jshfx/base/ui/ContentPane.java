package dev.jshfx.base.ui;

import java.nio.file.Path;

import dev.jshfx.jfx.file.FXPath;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentPane extends StackPane {

    protected final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    protected final ReadOnlyStringWrapper longTitle = new ReadOnlyStringWrapper();
    protected final ReadOnlyObjectWrapper<Node> graphic = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
    
    protected EventHandler<Event> onCloseRequest;
    
    ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    ReadOnlyStringProperty longTitleProperty() {
        return longTitle.getReadOnlyProperty();
    }
    
    ReadOnlyObjectProperty<Node> graphicProperty() {
    	return graphic.getReadOnlyProperty();
    }
    
    public void setOnCloseRequest(EventHandler<Event> value) {
        this.onCloseRequest = value;
    }
    
    public boolean isModified() {
        return modified.get();
    }
    
    public ReadOnlyBooleanProperty modifiedProperty() {
        return modified.getReadOnlyProperty();
    }
    
    public FXPath getFXPath() {
        return new FXPath();
    }
    
    public String getContent() {
        return "";
    }
    
    public void saved(Path path) {
        getFXPath().setPath(path);
    }
    
    public void setActions(Actions actions) {
        actions.setActions(this);
    }
    
    public void init() {}
    
    public void bind(Actions actions) {
        actions.bind(this);
    }
    
    public String getSelection() {
        return "";
    }
    
    public Finder getFinder() {
        return null;
    }
    
    public void activate() {}
    
    public void dispose() {
        modified.unbind();
        modified.set(false);
    }
}
