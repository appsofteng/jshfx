package dev.jshfx.fxmisc.richtext;

import java.util.List;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.scene.control.IndexRange;

public final class AreaUtils {
    
    private AreaUtils() {
    }

    public static int getParagraphForAbsolutePosition(GenericStyledArea<?, ?, ?> area, int position) {
        return new AreaWrapper<>(area).getParagraphForAbsolutePosition(position);
    }

    public static List<Integer> getParagraphs(GenericStyledArea<?, ?, ?> area, IndexRange range) {
        return new AreaWrapper<>(area).getParagraphs(range);
    }
}
