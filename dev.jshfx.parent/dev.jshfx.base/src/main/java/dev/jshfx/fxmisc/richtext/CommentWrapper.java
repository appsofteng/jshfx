package dev.jshfx.fxmisc.richtext;

import org.fxmisc.richtext.GenericStyledArea;

public final class CommentWrapper<T extends GenericStyledArea<?, ?, ?>> extends AreaWrapper<T> {

    public CommentWrapper(T area) {
        super(area);
    }

    public void toggleComment() {        
        changeParagraphs(this::toggleParagraph);
    }

    private String toggleParagraph(int index) {
        var text = area.getParagraph(index).getText();

        if (text.startsWith("//")) {
            text = text.substring(2);
        } else {
            text = "//" + text;
        }

        return text;
    }
}
