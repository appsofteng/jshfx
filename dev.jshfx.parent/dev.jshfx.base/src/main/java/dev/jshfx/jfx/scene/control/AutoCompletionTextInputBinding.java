package dev.jshfx.jfx.scene.control;

import java.util.Collection;

import org.controlsfx.control.textfield.AutoCompletionBinding;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextInputControl;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class AutoCompletionTextInputBinding<T> extends AutoCompletionBinding<T> {

    private static StringConverter<String> DEFAULT_CONVERTER = new DefaultStringConverter();

    private StringConverter<T> converter;

    private final ChangeListener<String> textChangeListener = (v, o, n) -> {
        if (getCompletionTarget().isFocused()) {
            setUserInput(n);
        }
    };

    private final ChangeListener<Boolean> focusChangedListener = (v, o, n) -> {

        if (!n) {
            hidePopup();
        }
    };

    @SuppressWarnings("unchecked")
    protected AutoCompletionTextInputBinding(TextInputControl completionTarget, Collection<T> suggestions) {
        this(completionTarget, suggestions, (StringConverter<T>) DEFAULT_CONVERTER);
    }

    protected AutoCompletionTextInputBinding(TextInputControl completionTarget, Collection<T> suggestions,
            StringConverter<T> converter) {
        super(completionTarget, new SuggestionProvider<>(suggestions, converter), (StringConverter<T>) converter);
        this.converter = converter;
        getCompletionTarget().textProperty().addListener(textChangeListener);
        getCompletionTarget().focusedProperty().addListener(focusChangedListener);
    }

    @Override
    public TextInputControl getCompletionTarget() {
        return (TextInputControl) super.getCompletionTarget();
    }

    @Override
    public void dispose() {
        getCompletionTarget().textProperty().removeListener(textChangeListener);
        getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
    }

    @Override
    protected void completeUserInput(T completion) {
        String newText = converter.toString(completion);
        getCompletionTarget().setText(newText);
        getCompletionTarget().positionCaret(newText.length());
    }

    private static class SuggestionProvider<T> implements Callback<ISuggestionRequest, Collection<T>> {

        private Collection<T> suggestions;
        private StringConverter<T> converter;

        public SuggestionProvider(Collection<T> suggestions, StringConverter<T> converter) {
            this.suggestions = suggestions;
            this.converter = converter;
        }

        @Override
        public Collection<T> call(ISuggestionRequest request) {
            return suggestions.stream().filter(t -> converter.toString(t).contains(request.getUserText())).toList();
        }
    }
}
