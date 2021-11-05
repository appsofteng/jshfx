package dev.jshfx.jfx.scene.control;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.fxmisc.wellbehaved.event.Nodes;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class AutoCompleteField<T> extends Region {

    private Collection<T> suggestions;
    private TextArea textArea;
    private AutoCompletionBinding<T> binding;
    @SuppressWarnings("unchecked")
    private StringConverter<T> converter = (StringConverter<T>) new DefaultStringConverter();
    private Consumer<T> onCompleted;
    private Consumer<T> onAction;
    private Runnable onCancel;

    public AutoCompleteField() {
        this("");
    }

    public AutoCompleteField(String text) {
        this(text, new TreeSet<>());
    }

    public AutoCompleteField(Collection<T> suggestions) {
        this("", suggestions);
    }

    public AutoCompleteField(String text, Collection<T> suggestions) {
        this.suggestions = suggestions;
        textArea = new TextArea();
        textArea.setPrefRowCount(5);
        textArea.setText(text);
        Platform.runLater(this::bindAutoCompletion);

        textArea.focusedProperty().addListener((v, o, n) -> {
            if (n) {
                Platform.runLater(() -> textArea.deselect());
            }
        });

        getChildren().add(textArea);

        Nodes.addInputMap(textArea, sequence(consume(keyPressed(KeyCode.ENTER).onlyIf(e -> true), e -> onSelected()),
                consume(keyPressed(KeyCode.ESCAPE).onlyIf(e -> true), e -> onCancel())));
    }

    private void bindAutoCompletion() {
        binding = new AutoCompletionTextInputBinding<>(textArea, suggestions);
        binding.setOnAutoCompleted(e -> onCompleted());
        binding.prefWidthProperty().bind(textArea.widthProperty());
    }

    private void onSelected() {

        if (store()) {
            if (onAction != null) {
                onAction.accept(converter.fromString(textArea.getText()));
            } else {
                onCompleted();
            }
        }
    }
    
    private void onCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    public boolean store() {
        if (textArea.getText().isBlank()) {
            return false;
        }

        suggestions.add(converter.fromString(textArea.getText()));

        if (binding != null) {
            binding.dispose();
        }

        bindAutoCompletion();

        return true;
    }

    public void setOnAction(Consumer<T> onAction) {
        this.onAction = onAction;
    }

    public void setOnCompleted(Consumer<T> consumer) {
        this.onCompleted = consumer;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }
    
    private void onCompleted() {
        if (onCompleted != null) {
            onCompleted.accept(converter.fromString(textArea.getText()));
        }
    }

    public StringProperty textProperty() {
        return textArea.textProperty();
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String value) {
        textArea.setText(value);
    }

    public StringProperty promptTextProperty() {
        return textArea.promptTextProperty();
    }

    @Override
    public void requestFocus() {
        textArea.requestFocus();
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(textArea, 0, 0, getWidth(), getHeight(), 0, new Insets(0), HPos.CENTER, VPos.CENTER);
    }
}
