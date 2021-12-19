package dev.jshfx.j.nio.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PathUtils {

    private PathUtils() {
    }

    public static Path relativize(Path path, Path other) {
        Path result = other;

        if (path != null) {
            result = path.relativize(other);
        }

        return result;
    }

    public static Path relativize(Collection<String> paths, Path other) {

        Path result = paths.stream().map(Path::of).filter(p -> other.startsWith(p)).findFirst()
                .map(p -> p.relativize(other)).orElse(other);

        return result;
    }

    public static Path resolve(Path path, String other) {
        Path result = Path.of(other);

        if (!result.isAbsolute() && path != null) {
            result = path.resolve(result);
        }

        return result;
    }

    public static Set<Path> resolve(Path path, Collection<String> paths, Collection<String> others) {
        return others.stream().map(p -> resolve(path, paths, p)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Path resolve(Path path, Collection<String> paths, String other) {

        Path otherPath = Path.of(other);
        Path result = otherPath;

        if (!otherPath.isAbsolute()) {

            if (otherPath.startsWith("/")) {
                Path p = Path.of("/").relativize(otherPath);
                result = paths.stream().map(d -> Path.of(d).resolve(p)).filter(Files::exists).findFirst()
                        .orElse(Path.of(paths.iterator().next()).resolve(otherPath));
            } else {
                if (path != null) {
                    result = path.resolve(otherPath);
                }
            }
        }

        return result;
    }

    public static Set<String> split(String path) {

        Set<String> result = Set.of();

        if (path != null && !path.isBlank()) {
            path = path.replaceFirst("\"(.*)\"", "$1");

            result = new LinkedHashSet<>(Arrays.asList(path.split(File.pathSeparator)));
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
