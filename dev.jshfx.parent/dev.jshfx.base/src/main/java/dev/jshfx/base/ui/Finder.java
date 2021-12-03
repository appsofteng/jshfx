package dev.jshfx.base.ui;

import java.util.regex.Pattern;

public interface Finder {

    String getSelection();

    void setScope(boolean inSelection);

    void findPrevious(Pattern pattern);

    void findNext(Pattern pattern);

    void replacePrevious(Pattern pattern, String replacement);

    void replaceNext(Pattern pattern, String replacement);

    void replaceAll(Pattern pattern, String replacement);
}
