package dev.jshfx.fxmisc.richtext;

public abstract class CompletionItem implements Comparable<CompletionItem> {

    public abstract void complete();

    public void completeStatic() {
    }

    public boolean isStatic() {
        return false;
    }

    @Override
    public int compareTo(CompletionItem o) {
        return toString().compareToIgnoreCase(o.toString());
    }
}
