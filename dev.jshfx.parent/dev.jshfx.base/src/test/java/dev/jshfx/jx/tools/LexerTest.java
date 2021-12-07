package dev.jshfx.jx.tools;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class LexerTest {

    @Test
    public void tokenizeJsh() throws Exception {
        Lexer lexer = Lexer.get("jsh");
        var input = Files.readString(Path.of("src/test/resources/sample.jsh"));
        var tokens = lexer.tokenize(input);
        var s = """
                
                """;
        System.out.println(tokens);
    }
}
