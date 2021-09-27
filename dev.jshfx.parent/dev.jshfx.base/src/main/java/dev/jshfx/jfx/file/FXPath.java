package dev.jshfx.jfx.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXPath {

    private ObjectProperty<Path> path = new SimpleObjectProperty<>();
    private StringProperty name = new SimpleStringProperty();
    private StringProperty fullName = new SimpleStringProperty();
    private ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private BooleanProperty directory = new SimpleBooleanProperty();
    
    public FXPath(String path) {
    	this(Path.of(path));
    }
    
    public FXPath(Path path) {
    	setListeners();
    	setPath(path);
    	
    	setDirectory(Files.isDirectory(path));
	}
    
    private void setListeners() {
        path.addListener((v, o, n) -> {
            if (n != null) {
                Path fileName = n.getFileName();
                setName(fileName == null ? n.toString() : fileName.toString());
                setFullName(n.toString());
            }
        });
    }
    
    public Path getPath() {
    	return path.get();
    }
    
    public void setPath(Path value) {
    	path.set(value);
    }
    
	public ReadOnlyObjectProperty<Path> pathProperty() {
		return path;
	}
    
    public String getName() {
    	return name.get();
    }
    
    public void setName(String value) {
    	name.set(value);
    }
    
    public StringProperty nameProperty() {
    	return name;
    }
    
    public String getFullName() {
    	return fullName.get();
    }
    
    public void setFullName(String value) {
    	fullName.set(value);
    }
    
    public StringProperty fullNameProperty() {
    	return fullName;
    }
    
    public void setImage(Image value) {
    	image.set(value);
    }
    
    public ObjectProperty<Image> imageProperty() {
        return image;
    }
    
    public boolean isDirectory() {
        return directory.get();
    }

    private void setDirectory(boolean value) {
        directory.set(value);
    }

    public ReadOnlyBooleanProperty directoryProperty() {
        return directory;
    }
    
    public Node getGraphic() {

        ImageView imageView = new ImageView();
        imageView.imageProperty().bind(image);
        Label label = new Label("", imageView);
        label.setContentDisplay(ContentDisplay.RIGHT);

        return label;
    }
    
    @Override
    public boolean equals(Object obj) {
        
    	if (obj instanceof FXPath otherPath) {
           return Objects.equals(getPath(), otherPath.getPath());
        } else {
        	return false;
        }
    }
    
    @Override
    public String toString() {
    	return getName();
    }
}
