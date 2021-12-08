package dev.jshfx.j.nio.file;

import java.nio.file.Path;
import java.util.function.Predicate;

public final class PathUtils {

    private PathUtils() {
    }
    
    public static Path relativize(Path parent, Path path) {       
        if (parent != null && path.startsWith(parent)) {
            path = parent.relativize(path);
        }
        
        return path;
    }

    public static String getUniqueName(String name, Predicate<String> used) {
        String result = name;
        int i = 1;
        
        while(used.test(result)) {
            result = name + i++;
        }
        
        return result;
    }
}
