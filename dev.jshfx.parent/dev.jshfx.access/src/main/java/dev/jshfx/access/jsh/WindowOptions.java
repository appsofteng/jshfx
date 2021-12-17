package dev.jshfx.access.jsh;

public final class WindowOptions {

    private String title = "";
    private int columns;
   
    public WindowOptions() {
    }
    
    public WindowOptions(String title, int columns) {
        this.title = title;
        this.columns = columns;
    }

    public String title() {
        return title;
    }

    public WindowOptions title(String title) {
        this.title = title;

        return this;
    }

    public int columns() {
        return columns;
    }

    public WindowOptions clumns(int columns) {
        this.columns = columns;

        return this;
    }
    
    @Override
    public String toString() {
        return String.format("%s %d", title, columns);
    }
}
