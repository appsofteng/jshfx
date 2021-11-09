package dev.jshfx.fxmisc.richtext;

import java.text.BreakIterator;

import org.fxmisc.richtext.CaretSelectionBind;
import org.fxmisc.richtext.CodeArea;

public class CustomCodeArea extends CodeArea {

    @Override
    public void selectWord() {
        if (getLength() == 0)
            return;

        CaretSelectionBind<?, ?, ?> csb = getCaretSelectionBind();
        int paragraph = csb.getParagraphIndex();
        int position = csb.getColumnPosition();

        String paragraphText = getText(paragraph);
        BreakIterator breakIterator = BreakIterator.getWordInstance(getLocale());
        breakIterator.setText(paragraphText);

        breakIterator.preceding(position);
        int start = breakIterator.current();

        while (start > 0 && paragraphText.charAt(start - 1) == '_') {
            if (--start > 0 && !breakIterator.isBoundary(start - 1)) {
                breakIterator.preceding(start);
                start = breakIterator.current();
            }
        }

        breakIterator.following(position);
        int end = breakIterator.current();
        int len = paragraphText.length();

        while (end < len && paragraphText.charAt(end) == '_') {
            if (++end < len && !breakIterator.isBoundary(end + 1)) {
                breakIterator.following(end);
                end = breakIterator.current();
            }
            // For some reason single digits aren't picked up so ....
            else if (Character.isDigit(paragraphText.charAt(end))) {
                end++;
            }
        }

        int i = paragraphText.substring(start, position).lastIndexOf('.');

        if (i > -1) {
            start += i + 1;
        }

        i = paragraphText.substring(position, end).indexOf('.');
        
        if (i > -1) {
            end = position + i;
        }

        csb.selectRange(paragraph, start, paragraph, end);
    }

}
