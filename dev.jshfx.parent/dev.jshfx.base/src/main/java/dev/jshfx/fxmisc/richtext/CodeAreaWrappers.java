package dev.jshfx.fxmisc.richtext;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.EventPattern.mousePressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.tools.Diagnostic;

import org.apache.commons.io.FilenameUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.Nodes;

import dev.jshfx.jx.tools.Lexer;
import dev.jshfx.jx.tools.Token;
import javafx.application.Platform;

public final class CodeAreaWrappers {

    private CodeArea area;
    private String fileName;
    private String language;
    private Lexer lexer;

    private FindWrapper findWrapper;
    private CompilationWrapper compilationWrapper;

    private CodeAreaWrappers(CodeArea area, String fileName, String language) {
        this.area = area;
        this.fileName = fileName;
        this.language = language;
    }

    public FindWrapper getFindWrapper() {
        return findWrapper;
    }

    public CompilationWrapper getCompilationWrapper() {
        return compilationWrapper;
    }

    public static CodeAreaWrappers get(CodeArea area, String language) {
        return new CodeAreaWrappers(area, language, language);
    }

    public static CodeAreaWrappers get(CodeArea area, Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return new CodeAreaWrappers(area, fileName, FilenameUtils.getExtension(fileName));
    }

    public Lexer getLexer() {

        if (lexer == null) {
            lexer = Lexer.get(fileName, language);
        }

        return lexer;
    }

    public CodeAreaWrappers style() {
        var languegeSyle = CodeAreaWrappers.class.getResource(language + ".css");
        if (languegeSyle != null) {
            area.getStylesheets().add(languegeSyle.toExternalForm());
        }
        area.getStylesheets().add(CodeAreaWrappers.class.getResource("area.css").toExternalForm());

        if (area.isEditable()) {
            area.getStylesheets().add(CodeAreaWrappers.class.getResource("edit.css").toExternalForm());
        }

        return this;
    }

    public CodeAreaWrappers highlighting() {
        return highlighting(new AtomicBoolean());
    }

    public CodeAreaWrappers highlighting(AtomicBoolean disableHighlight) {

        if (getLexer() == null) {
            return this;
        }

        var blockEndWrapper = new BlockEndWrapper<>(area);
        var highlightWrapper = new HighlightWrapper(area, getLexer());

        area.richChanges()
                .filter(ch -> !ch.toPlainTextChange().getInserted().equals(ch.toPlainTextChange().getRemoved()))
                .successionEnds(Duration.ofMillis(100)).subscribe(ch -> {
                    if (disableHighlight.get()) {
                        return;
                    }

                    if (compilationWrapper != null) {
                        compilationWrapper.compile();
                    }

                    var plainChange = ch.toPlainTextChange();
                    int insertionEnd = plainChange.getInsertionEnd();

                    highlight(highlightWrapper);

                    var tokenOnCaret = getLexer().getTokensOnCaretPosition().stream().filter(Token::isClose).findFirst()
                            .orElse(null);

                    if (insertionEnd == area.getCaretPosition() && tokenOnCaret != null
                            && tokenOnCaret.getValue().equals(plainChange.getInserted())) {
                        blockEndWrapper.indentEnd(tokenOnCaret);
                    }

                    if (findWrapper != null) {
                        findWrapper.afterReplace();
                    }

                    if (compilationWrapper != null) {
                        compilationWrapper.showDiags();
                    }
                });

        highlight(highlightWrapper);
        
        area.caretPositionProperty().addListener((v, o, n) -> {
            Platform.runLater(() -> highlightWrapper.highlightDelimiters(n));
        });

        return this;
    }

    private CodeAreaWrappers highlight(HighlightWrapper highlightWrapper) {

        int caretPosition = area.getCaretPosition();

        // Use List not StyleSpansBuilder, StyleSpansBuilder merges styles immediately.
        List<StyleSpan<Collection<String>>> spans = new ArrayList<>();

        var end = getLexer().tokenize(area.getText(), caretPosition, (lastEnd, t) -> {
            spans.add(new StyleSpan<>(Collections.emptyList(), t.getStart() - lastEnd));
            StyleSpan<Collection<String>> styleSpan = new StyleSpan<>(t.getStyle(), t.getLength());
            spans.add(styleSpan);
        });

        highlightWrapper.setToken(getLexer().getTokensOnCaretPosition());
        highlightWrapper.setAreaLength(area.getLength());

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.addAll(spans);
        spansBuilder.add(Collections.emptyList(), area.getText().length() - end);
        StyleSpans<Collection<String>> styleSpans = spansBuilder.create();

        area.setStyleSpans(0, styleSpans);

        return this;
    }

    public CodeAreaWrappers indentation() {
        IndentationWrapper<GenericStyledArea<?, ?, ?>> indentationWrapper = new IndentationWrapper<>(area, getLexer());
        Nodes.addInputMap(area,
                sequence(consume(keyPressed(ENTER), e -> indentationWrapper.insertNewLineIndentation()),
                        consume(keyPressed(TAB), e -> indentationWrapper.insertIndentation()),
                        consume(keyPressed(TAB, SHIFT_DOWN), e -> indentationWrapper.deleteIndentation())));

        return this;
    }

    public CodeAreaWrappers find() {

        findWrapper = new FindWrapper(area);

        Nodes.addInputMap(area, sequence(
                consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> findWrapper.findWord())));

        return this;
    }

    public CodeAreaWrappers compile(Supplier<CompletableFuture<List<Diagnostic<Path>>>> supplier) {
        this.compilationWrapper = new CompilationWrapper(area, supplier);

        return this;
    }
}
