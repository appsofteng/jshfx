package dev.jshfx.base.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class Part extends StackPane {

    protected final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    protected final ReadOnlyStringWrapper longTitle = new ReadOnlyStringWrapper();
    protected final ReadOnlyObjectWrapper<Node> graphic = new ReadOnlyObjectWrapper<>();
    
    public Part() {
	}
    
    ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    ReadOnlyStringProperty longTitleProperty() {
        return longTitle.getReadOnlyProperty();
    }
    
    ReadOnlyObjectProperty<Node> graphicProperty() {
    	return graphic.getReadOnlyProperty();
    }
}
