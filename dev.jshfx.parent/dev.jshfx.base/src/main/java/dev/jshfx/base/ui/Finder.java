package dev.jshfx.base.ui;

import java.util.regex.Pattern;

public interface Finder {

    void findPrevious(Pattern pattern, boolean inSelection);
    void findNext(Pattern pattern, boolean inSelection);
    
    void replacePrevious(Pattern pattern, boolean inSelection);
    void replaceNext(Pattern pattern, boolean inSelection);
    
    void replaceAll(Pattern pattern, boolean inSelection);
}
