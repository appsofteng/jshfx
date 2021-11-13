package dev.jshfx.base.jshell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public final class JShellUtils {

    private JShellUtils() {
    }

    public record LineSpan(String text, String originalText, int firstParagraphIndex, int lastParagraphIndex, int caretPosition) {
    }

    public static LineSpan getCurrentLineSpan(CodeArea area) {
        return getCurrentLineSpan(area, false);
    }
    
    public static LineSpan getCurrentLineSpan(CodeArea area, boolean keepLength) {
        int i = area.getCurrentParagraph();
        String replacement = keepLength ? " " : "";

        for (int j = i - 1; j >= 0; j--) {
            if (!CommandProcessor.isCommand(area.getParagraph(j).getText()) || !area.getParagraph(j).getText().endsWith("\\")) {
                break;
            } else {
                i = j;
            }
        }
        
        int caretPosition = area.getCaretPosition() - area.getAbsolutePosition(i, 0); 

        String text = area.getParagraph(i).getText();
        String originalText = text;
        int firstParagraphIndex = i;

        if (CommandProcessor.isCommand(text)) {

            while (text.endsWith("\\") && i < area.getParagraphs().size()) {
                text = text.substring(0, text.length() - 1) + replacement;
                String nextParagraph = area.getParagraph(++i).getText();
                originalText += "\n" + nextParagraph;
                if (nextParagraph.startsWith("/")) {
                    text += replacement + replacement + nextParagraph.substring(1);
                } else {
                    i--;
                }
            }
        }

        return new LineSpan(text, originalText, firstParagraphIndex, i, caretPosition);
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
