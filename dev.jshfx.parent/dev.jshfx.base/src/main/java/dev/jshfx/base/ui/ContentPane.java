package dev.jshfx.base.ui;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentPane extends StackPane {

    protected final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    protected final ReadOnlyStringWrapper longTitle = new ReadOnlyStringWrapper();
    protected final ReadOnlyObjectWrapper<Node> graphic = new ReadOnlyObjectWrapper<>();
    protected ReadOnlyBooleanWrapper closed = new ReadOnlyBooleanWrapper();
    
    ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    ReadOnlyStringProperty longTitleProperty() {
        return longTitle.getReadOnlyProperty();
    }
    
    ReadOnlyObjectProperty<Node> graphicProperty() {
    	return graphic.getReadOnlyProperty();
    }
    
    public ReadOnlyBooleanProperty closedProperty() {
        return closed.getReadOnlyProperty();
    }
    
    public void activate() {}
    
    public void dispose() {}
}
