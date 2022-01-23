package dev.jshfx.j.nio.file;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class FileUtils {

    private FileUtils() {
    }

    public static String readString(Path path) throws IOException {
        String string = null;
        Deque<Charset> charsets = new ArrayDeque<>(
                List.of(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_16));

        while (!charsets.isEmpty()) {
            try {
                Charset charset = charsets.pop();
                string = Files.readString(path, charset);
                charsets.clear();
            } catch (MalformedInputException e) {
                if (charsets.isEmpty()) {
                    throw e;
                }
            }
        }
        
        return string;
    }
}
