package dev.jshfx.jx.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class LexerTest {

    @Test
    public void tokenizeJsh() throws Exception {
        Lexer lexer = Lexer.get("jsh");
        var input = Files.readString(Path.of("src/test/resources/sample.jsh"));
        var tokens = lexer.tokenize(input);
        System.out.println(tokens);
    }

    @Test
    public void tokenizeCommand() throws Exception {
        Lexer lexer = Lexer.get("java");
        var input = "/l \\\n/a";
        var tokens = lexer.tokenize(input);
        // assertEquals(GroupNames.JSHELLCOMMAND, tokens.get(0).getType());
        System.out.println(tokens);
    }

    @Test
    public void tokenizeCommandArgs() throws Exception {
        Lexer lexer = Lexer.get("commands");
        var input = "/l a \\\n/b";
        var tokens = lexer.tokenize(input);
        // assertEquals(GroupNames.JSHELLCOMMAND, tokens.get(0).getType());
        System.out.println(tokens);
    }
    
    @Test
    public void tokenizeComment() throws Exception {
        Lexer lexer = Lexer.get("java");
        var input = "/* \n/comm */";
        var tokens = lexer.tokenize(input);
        // assertEquals(GroupNames.JSHELLCOMMAND, tokens.get(0).getType());
        System.out.println(tokens);
        //  // [^\n]*|/\*(.|\R)*?\*/|/\*[^\v]*|^\h*\*([^\v]*|/)
    }
}
