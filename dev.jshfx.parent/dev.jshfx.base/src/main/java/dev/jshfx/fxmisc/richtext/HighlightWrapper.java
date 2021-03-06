package dev.jshfx.fxmisc.richtext;

import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jshfx.jx.tools.Lexer;
import dev.jshfx.jx.tools.Token;

public class HighlightWrapper extends StyleClassedTextAreaWrapper {

    private Token token;
    private Lexer lexer;
    private int areaLength;

    public HighlightWrapper(CodeArea area, Lexer lexer) {
        super(area);
        this.lexer = lexer;
        this.areaLength = area.getLength();
    }

    void highlightDelimiters(int caretPosition) {

        if (area.getLength() != areaLength) {
            areaLength = area.getLength();
            token = null;
            return;
        }

        if (token != null) {
            removeHighlightDelimiter();
        }

        token = lexer.getTokensNextToCarretPosition(caretPosition).stream().filter(Token::isDelimiter).findFirst().orElse(null);
        if (token != null) {
            highlightDelimiter();
        }
    }

    private void highlightDelimiter() {
        if (token.isDelimiter()) {
// Don't do this because the CodeArea merges the styles of adjacent segments and thus changes color of all the adjacent delimiters.
//            token.getStyle().add("block-delimiter-match");
//            token.getOppositeToken().getStyle().add("block-delimiter-match");
            addStyle(token.getStart(), token.getEnd(), List.of("block-delimiter-match"));
            addStyle(token.getOppositeToken().getStart(), token.getOppositeToken().getEnd(), List.of("block-delimiter-match"));
        }
    }

    private void removeHighlightDelimiter() {
        if (token.isDelimiter()) {
//            token.resetStyle();
//            token.getOppositeToken().resetStyle();
            removeStyle(token.getStart(), token.getEnd(), List.of("block-delimiter-match"));
            removeStyle(token.getOppositeToken().getStart(), token.getOppositeToken().getEnd(), List.of("block-delimiter-match"));
        }
    }

    void setAreaLength(int areaLength) {
        this.areaLength = areaLength;
    }

    void setToken(List<Token> tokens) {
        
        var token = tokens.stream().filter(Token::isDelimiter).findFirst().orElse(null);

        if (token != null) {

            token.getStyle().add("block-delimiter-match");
            token.getOppositeToken().getStyle().add("block-delimiter-match");
            this.token = token;
        }
    }
}
