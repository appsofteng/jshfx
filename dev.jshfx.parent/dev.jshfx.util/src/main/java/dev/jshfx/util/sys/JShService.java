package dev.jshfx.util.sys;

import java.nio.file.Path;

import dev.jshfx.util.jsh.WindowContent;

public interface JShService {
    
    Path getCurDir() ;
    void show(WindowContent content);
}
