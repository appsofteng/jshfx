package dev.jshfx.base.ui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.StyleClassedTextAreaWrapper;
import javafx.scene.control.IndexRange;

class FinderImpl implements Finder {

    private static final String FIND_STYLE = "jsh-find";

    private StyleClassedTextAreaWrapper areaWrapper;
    private boolean inSelection;
    private List<Integer> selectionParagraphs = List.of();

    public FinderImpl(CodeArea area) {
        this.areaWrapper = new StyleClassedTextAreaWrapper(area);

        area.focusedProperty().addListener((v, o, n) -> {
            if (n) {
                selectionParagraphs.forEach(i -> areaWrapper.getArea().clearParagraphStyle(i));
                selectionParagraphs = List.of();
                inSelection = false;
            }
        });
    }
    
    @Override
    public String getSelection() {
        return areaWrapper.getArea().getSelectedText();
    }

    @Override
    public void setScope(boolean inSelection) {
        this.inSelection = inSelection;
        if (inSelection) {
            if (selectionParagraphs.isEmpty()) {
                var selectionRange = areaWrapper.getArea().getSelection();
                if (selectionRange.getLength() == 0) {
                    int currentParagraph = areaWrapper.getArea().getCurrentParagraph();
                    int start = areaWrapper.getArea().getAbsolutePosition(currentParagraph, 0);
                    int end = areaWrapper.getArea().getAbsolutePosition(currentParagraph,
                            areaWrapper.getArea().getParagraphLength(currentParagraph));
                    selectionRange = new IndexRange(start, end);
                }
                selectionParagraphs = areaWrapper.getSelectedParagraphs(selectionRange);
                areaWrapper.getArea().deselect();
            }

            selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
            areaWrapper.getArea().moveTo(selectionParagraphs.get(0), 0);
            areaWrapper.getArea().requestFollowCaret();
        } else {
            selectionParagraphs.forEach(i -> areaWrapper.getArea().clearParagraphStyle(i));
        }
    }

    private int getOffset() {
        return inSelection ? areaWrapper.getArea().getAbsolutePosition(selectionParagraphs.get(0), 0) : 0;
    }

    private int getPosition() {
        return inSelection ? areaWrapper.getArea().getCaretPosition() - getOffset()
                : areaWrapper.getArea().getCaretPosition();
    }

    private String getInput() {
        return inSelection ? selectionParagraphs.stream().map(i -> areaWrapper.getArea().getParagraph(i).getText())
                .collect(Collectors.joining("\n")) : areaWrapper.getArea().getText();
    }

    @Override
    public void findPrevious(Pattern pattern) {

        Matcher matcher = pattern.matcher(getInput());
        int start = -1;
        int end = 0;

        var selection = areaWrapper.getArea().getSelection();

        if (selection.getLength() == 0) {
            selection = new IndexRange(areaWrapper.getArea().getCaretPosition(),
                    areaWrapper.getArea().getCaretPosition());
        }

        int offset = getOffset();

        while (matcher.find() && matcher.start() < selection.getStart() - offset) {
            start = matcher.start();
            end = matcher.end();
        }

        if (start > -1) {
            areaWrapper.getArea().selectRange(start + offset, end + offset);
        } else {
            while (matcher.find(getPosition())) {
                start = matcher.start();
                end = matcher.end();
                areaWrapper.getArea().moveTo(end + offset);                
            }

            if (start > -1) {
                areaWrapper.getArea().selectRange(start + offset, end + offset);
            }
        }
        
        areaWrapper.getArea().requestFollowCaret();
    }

    @Override
    public void findNext(Pattern pattern) {

        Matcher matcher = pattern.matcher(getInput());
        int offset = getOffset();

        if (matcher.find(getPosition())) {
            areaWrapper.getArea().selectRange(matcher.start() + offset, matcher.end() + offset);
        } else {
            if (matcher.find(0)) {
                areaWrapper.getArea().selectRange(matcher.start() + offset, matcher.end() + offset);
            }
        }
        
        areaWrapper.getArea().requestFollowCaret();
    }

    @Override
    public void replacePrevious(Pattern pattern, String replacement) {
        String selection = areaWrapper.getArea().getSelectedText();

        if (selection.matches(pattern.pattern())) {
            areaWrapper.getArea().replaceSelection(replacement);
            if (inSelection) {
                selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
            }
        }

        findPrevious(pattern);

    }

    @Override
    public void replaceNext(Pattern pattern, String replacement) {
        String selection = areaWrapper.getArea().getSelectedText();

        if (selection.matches(pattern.pattern())) {
            areaWrapper.getArea().replaceSelection(replacement);
            if (inSelection) {
                selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
            }
        }

        findNext(pattern);
    }

    @Override
    public void replaceAll(Pattern pattern, String replacement) {
        Matcher matcher = pattern.matcher(getInput());

        var result = matcher.replaceAll(replacement);

        if (inSelection) {
            int lastParagraph = selectionParagraphs.get(selectionParagraphs.size() - 1);
            areaWrapper.getArea().replaceText(selectionParagraphs.get(0), 0, lastParagraph,
                    areaWrapper.getArea().getParagraphLength(lastParagraph), result);
            selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
        } else {
            areaWrapper.getArea().replaceText(result);
        }
    }
}
