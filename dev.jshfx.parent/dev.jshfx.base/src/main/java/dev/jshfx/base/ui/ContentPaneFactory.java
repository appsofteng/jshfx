package dev.jshfx.base.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import dev.jshfx.j.nio.file.XFiles;

public class ContentPaneFactory {

    private static final String JAVA = "java";
    private static final String JSH = "jsh";
    private static final List<String> EXTENSIONS = List.of(JAVA, JSH);
    private static final List<String> SHELL_EXTENSIONS = List.of(JAVA, JSH);
    
    
    public ContentPane newShellPane(String name) {        
        return new ShellPane(name);
    }
    public List<ContentPane> create(List<Path> paths) {
        
        var panes = paths.stream()
        .filter(p -> EXTENSIONS.contains(XFiles.getFileExtension(p)))
        .map(this::create)
        .collect(Collectors.toList());
        
        return panes;
    }
    
    private ContentPane create(Path path) {
        ContentPane pane = null;
        
        if (SHELL_EXTENSIONS.contains(XFiles.getFileExtension(path))) {
            try {
                String input = Files.readString(path);
                pane = new ShellPane(path, input);
            } catch (IOException e) {              
                e.printStackTrace();
            }          
        }
        
        return pane;
    }
}
