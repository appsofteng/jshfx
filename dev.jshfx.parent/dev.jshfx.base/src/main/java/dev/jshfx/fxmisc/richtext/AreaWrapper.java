package dev.jshfx.fxmisc.richtext;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.IndexRange;

public class AreaWrapper<T extends GenericStyledArea<?, ?, ?>> {

    protected T area;
    
    private ReadOnlyBooleanWrapper allSelected = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper clear = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper selectionEmpty = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper redoEmpty = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper undoEmpty = new ReadOnlyBooleanWrapper();

    public AreaWrapper(T area) {
        this.area = area;
        
        allSelected.bind(Bindings.createBooleanBinding(
                () -> area.getSelectedText().length() == area.getText().length(),
                area.selectedTextProperty()));

        selectionEmpty.bind(Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0,
                area.selectionProperty()));

        clear.bind(Bindings.createBooleanBinding(() -> area.getLength() == 0,
                area.lengthProperty()));

        redoEmpty.bind(Bindings.createBooleanBinding(() -> !area.isRedoAvailable(),
                area.redoAvailableProperty()));
        undoEmpty.bind(Bindings.createBooleanBinding(() -> !area.isUndoAvailable(),
                area.undoAvailableProperty()));
    }

    public T getArea() {
        return area;
    }
    
    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected.getReadOnlyProperty();
    }
    
    public ReadOnlyBooleanProperty clearProperty() {
        return clear.getReadOnlyProperty();
    }
    
    public ReadOnlyBooleanProperty selectionEmptyProperty() {
        return selectionEmpty.getReadOnlyProperty();
    }
    
    public ReadOnlyBooleanProperty redoEmptyProperty() {
        return redoEmpty.getReadOnlyProperty();
    }
    
    public ReadOnlyBooleanProperty undoEmptyProperty() {
        return undoEmpty.getReadOnlyProperty();
    }

    boolean isCaretPosition(int position, int insertionEnd) {
        int caretPosition = insertionEnd >= 0 ? insertionEnd : getArea().getCaretPosition();
        boolean isCaretPosition = position == caretPosition || position == caretPosition - 1;
        return isCaretPosition;
    }

    String getCurrentParagraphText() {
        return getArea().getText(getArea().getCurrentParagraph());
    }

    String getCurrentParagraphIndentation() {
        return getParagraphIndentation(getArea().getCurrentParagraph());
    }

    String getParagraphIndentation(int index) {
        String text = getArea().getText(index);
        Matcher matcher = Pattern.compile("( *).*").matcher(text);
        String indentation = matcher.find() ? matcher.group(1) : "";

        return indentation;
    }

    boolean isCaretInIndentation() {
        return getCurrentParagraphText().substring(0, getArea().getCaretColumn()).trim().isEmpty();
    }

    public int getParagraphForAbsolutePosition(int position) {
        return getArea().offsetToPosition(position, Forward).getMajor();
    }

    int getColumnForAbsolutePosition(int position) {
        return getArea().offsetToPosition(position, Forward).getMinor();
    }

    public List<Integer> getParagraphs(IndexRange range) {
        List<Integer> paragraphs = List.of();

        if (range.getLength() > 0) {
            int startParagraph = getParagraphForAbsolutePosition(range.getStart());
            int endParagraph = getParagraphForAbsolutePosition(range.getEnd());
            paragraphs = new ArrayList<>();
            for (int i = startParagraph; i <= endParagraph; i++) {
                paragraphs.add(i);
            }
        }

        return paragraphs;
    }
    
    public String getParagraphText(IndexRange range) {
        return getParagraphs(range).stream().map(i -> area.getParagraph(i).getText()).collect(Collectors.joining("\n"));
    }

    void changeParagraphs(Function<Integer, String> change) {

        IndexRange selectionRange = getArea().getSelection();
        IndexRange range = selectionRange;

        if (selectionRange.getLength() == 0) {
            int i = area.getCurrentParagraph();
            int start = area.getAbsolutePosition(i, 0);
            int end = area.getAbsolutePosition(i, area.getParagraphLength(i));
            range = new IndexRange(start, end);
        }

        int startParagraph = getParagraphForAbsolutePosition(range.getStart());
        int endParagraph = getParagraphForAbsolutePosition(range.getEnd());
        boolean caretAtEnd = getArea().getCaretPosition() == range.getEnd();

        StringBuilder builder = new StringBuilder();

        for (int i = startParagraph; i <= endParagraph; i++) {
            builder.append(change.apply(i));
            if (i < endParagraph) {
                builder.append("\n");
            }
        }

        String oldText = getArea().getText(range);
        String newText = builder.toString();

        if (oldText.equals(newText)) {
            return;
        }

        getArea().replaceText(getArea().getAbsolutePosition(startParagraph, 0),
                getArea().getAbsolutePosition(endParagraph, getArea().getParagraphLength(endParagraph)), newText);

        if (selectionRange.getLength() > 0) {
            if (caretAtEnd) {
                getArea().selectRange(startParagraph, 0, endParagraph, getArea().getParagraphLength(endParagraph));
            } else {
                getArea().selectRange(endParagraph, getArea().getParagraphLength(endParagraph), startParagraph, 0);
            }
        }
    }
}
