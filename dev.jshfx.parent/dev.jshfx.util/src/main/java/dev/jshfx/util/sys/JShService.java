package dev.jshfx.util.sys;

import java.nio.file.Path;

public interface JShService {
    
    Path getCurDir() ;
    void show(WindowContent content);
}
