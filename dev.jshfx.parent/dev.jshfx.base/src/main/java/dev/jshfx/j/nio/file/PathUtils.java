package dev.jshfx.j.nio.file;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Predicate;

import org.apache.commons.io.FilenameUtils;

public final class PathUtils {

    private PathUtils() {
    }

    public static Path relativize(Path parent, Path path) {
        if (parent != null && parent.isAbsolute()) {
            path = parent.relativize(path);
        }

        return path;
    }

    public static Path resolve(Path curDir, Collection<String> dirs, String pathStr) {

        Path path = Path.of(pathStr);
        Path result = path;

        if (!path.isAbsolute()) {

            if (FilenameUtils.separatorsToUnix(pathStr).startsWith("/")) {
                Path p = Path.of(pathStr.substring(1));
                result = dirs.stream().map(d -> Path.of(d).resolve(p)).findFirst().orElse(path);
            } else {
                result = curDir.resolve(path);
            }
        }

        return result;
    }

    public static String getUniqueName(String name, Predicate<String> used) {
        String result = name;
        int i = 1;

        while (used.test(result)) {
            result = name + i++;
        }

        return result;
    }
}
