package dev.jshfx.base.ui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.AreaUtils;
import javafx.scene.control.IndexRange;

class FinderImpl implements Finder {

    private static final String FIND_STYLE = "jsh-find";

    private CodeArea area;
    private boolean inSelection;
    private List<Integer> selectionParagraphs = List.of();

    public FinderImpl(CodeArea area) {
        this.area = area;

        area.focusedProperty().addListener((v, o, n) -> {
            if (n) {
                selectionParagraphs.forEach(i -> area.clearParagraphStyle(i));
                selectionParagraphs = List.of();
                inSelection = false;
            }
        });
    }
    
    @Override
    public String getSelection() {
        return area.getSelectedText();
    }

    @Override
    public void setScope(boolean inSelection) {
        this.inSelection = inSelection;
        if (inSelection) {
            if (selectionParagraphs.isEmpty()) {
                var selectionRange = area.getSelection();
                if (selectionRange.getLength() == 0) {
                    int currentParagraph = area.getCurrentParagraph();
                    int start = area.getAbsolutePosition(currentParagraph, 0);
                    int end = area.getAbsolutePosition(currentParagraph,
                            area.getParagraphLength(currentParagraph));
                    selectionRange = new IndexRange(start, end);
                }
                selectionParagraphs = AreaUtils.getParagraphs(area, selectionRange);
                area.deselect();
            }

            selectionParagraphs.forEach(i -> area.setParagraphStyle(i, List.of(FIND_STYLE)));
            area.moveTo(selectionParagraphs.get(0), 0);
            area.requestFollowCaret();
        } else {
            selectionParagraphs.forEach(i -> area.clearParagraphStyle(i));
        }
    }

    private int getOffset() {
        return inSelection ? area.getAbsolutePosition(selectionParagraphs.get(0), 0) : 0;
    }

    private int getPosition() {
        return inSelection ? area.getCaretPosition() - getOffset()
                : area.getCaretPosition();
    }

    private String getInput() {
        return inSelection ? selectionParagraphs.stream().map(i -> area.getParagraph(i).getText())
                .collect(Collectors.joining("\n")) : area.getText();
    }

    @Override
    public void findPrevious(Pattern pattern) {

        Matcher matcher = pattern.matcher(getInput());
        int start = -1;
        int end = 0;

        var selection = area.getSelection();

        if (selection.getLength() == 0) {
            selection = new IndexRange(area.getCaretPosition(),
                    area.getCaretPosition());
        }

        int offset = getOffset();

        while (matcher.find() && matcher.start() < selection.getStart() - offset) {
            start = matcher.start();
            end = matcher.end();
        }

        if (start > -1) {
            area.selectRange(start + offset, end + offset);
        } else {
            while (matcher.find(getPosition())) {
                start = matcher.start();
                end = matcher.end();
                area.moveTo(end + offset);                
            }

            if (start > -1) {
                area.selectRange(start + offset, end + offset);
            }
        }
        
        area.requestFollowCaret();
    }

    @Override
    public void findNext(Pattern pattern) {

        Matcher matcher = pattern.matcher(getInput());
        int offset = getOffset();

        if (matcher.find(getPosition())) {
            area.selectRange(matcher.start() + offset, matcher.end() + offset);
        } else {
            if (matcher.find(0)) {
                area.selectRange(matcher.start() + offset, matcher.end() + offset);
            }
        }
        
        area.requestFollowCaret();
    }

    @Override
    public void replacePrevious(Pattern pattern, String replacement) {
        String selection = area.getSelectedText();

        if (selection.matches(pattern.pattern())) {
            area.replaceSelection(replacement);
            if (inSelection) {
                selectionParagraphs.forEach(i -> area.setParagraphStyle(i, List.of(FIND_STYLE)));
            }
        }

        findPrevious(pattern);

    }

    @Override
    public void replaceNext(Pattern pattern, String replacement) {
        String selection = area.getSelectedText();

        if (selection.matches(pattern.pattern())) {
            area.replaceSelection(replacement);
            if (inSelection) {
                selectionParagraphs.forEach(i -> area.setParagraphStyle(i, List.of(FIND_STYLE)));
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
            area.replaceText(selectionParagraphs.get(0), 0, lastParagraph,
                    area.getParagraphLength(lastParagraph), result);
            selectionParagraphs.forEach(i -> area.setParagraphStyle(i, List.of(FIND_STYLE)));
        } else {
            area.replaceText(result);
        }
    }
}
