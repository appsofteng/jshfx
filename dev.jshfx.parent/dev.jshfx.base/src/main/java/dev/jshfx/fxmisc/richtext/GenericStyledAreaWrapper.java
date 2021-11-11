package dev.jshfx.fxmisc.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.Paragraph;

import javafx.scene.control.IndexRange;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

public abstract class GenericStyledAreaWrapper<T extends GenericStyledArea<?, ?, ?>> {

    protected T area;

    public GenericStyledAreaWrapper(T area) {
        this.area = area;
    }

    public T getArea() {
        return area;
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

    int getParagraphForAbsolutePosition(int position) {
        return getArea().offsetToPosition(position, Forward).getMajor();
    }

    int getColumnForAbsolutePosition(int position) {
        return getArea().offsetToPosition(position, Forward).getMinor();
    }

    public List<Integer> getSelectedParagraphs(IndexRange selectionRange) {
        List<Integer> paragraphs = List.of();

        if (selectionRange.getLength() > 0) {
            int startParagraph = getParagraphForAbsolutePosition(selectionRange.getStart());
            int endParagraph = getParagraphForAbsolutePosition(selectionRange.getEnd());
            paragraphs = new ArrayList<>();
            for (int i = startParagraph; i <= endParagraph; i++) {
                paragraphs.add(i);
            }
        }

        return paragraphs;
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
