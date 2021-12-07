package dev.jshfx.base.jshell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public final class JShellUtils {

    private JShellUtils() {
    }

    public static Snippet getSnippet(JShell jshell, Integer id) {
        Snippet snippet = jshell.snippets().filter(s -> s.id().equals(id.toString())).findFirst().orElse(null);

        return snippet;
    }

    public static void loadSnippets(JShell jshell, InputStream in) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            reader.lines().forEach(s -> jshell.eval(s));
        }
    }
}
