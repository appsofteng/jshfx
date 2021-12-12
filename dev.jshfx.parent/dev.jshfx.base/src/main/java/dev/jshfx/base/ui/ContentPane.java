package dev.jshfx.base.ui;

import java.nio.file.Path;

import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import dev.jshfx.jfx.file.FXPath;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;

public class ContentPane extends EnvPane {

    protected final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    protected final ReadOnlyStringWrapper longTitle = new ReadOnlyStringWrapper();
    protected final ReadOnlyObjectWrapper<Node> graphic = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
    protected final ReadOnlyStringWrapper consoleHeaderText = new ReadOnlyStringWrapper();
    protected ConsoleModel consoleModel = new ConsoleModel();
    private FXPath fxpath;

    protected EventHandler<Event> onCloseRequest;

    public ContentPane(Path p) {
        this.fxpath = new FXPath(p);

        title.bind(Bindings.createStringBinding(() -> createTitle(), fxpath.nameProperty(), modifiedProperty()));
        longTitle.bind(Bindings.createStringBinding(() -> fxpath.getPath().toString(), fxpath.pathProperty()));
    }

    @Override
    public void setActions(Actions actions) {
        actions.setActions(this);
    }

    @Override
    public void bindActions(Actions actions) {
        actions.getSaveAction().disabledProperty().bind(modifiedProperty().not());
    }

    private String createTitle() {
        String result = isModified() ? "*" + fxpath.getName() : fxpath.getName();

        return result;
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

    ReadOnlyStringProperty consoleHeaderTextProperty() {
        return consoleHeaderText.getReadOnlyProperty();
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
        return fxpath;
    }

    public String getContent() {
        return "";
    }

    public void saved(Path path) {
        getFXPath().setPath(path);
    }

    public void init() {
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public void activate() {
        requestFocus();
    }

    public void dispose() {
        modified.unbind();
        modified.set(false);
        consoleModel.dispose();
    }
}
