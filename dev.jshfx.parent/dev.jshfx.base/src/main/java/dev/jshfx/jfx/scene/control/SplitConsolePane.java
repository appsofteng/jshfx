package dev.jshfx.jfx.scene.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class SplitConsolePane extends BorderPane {

    private static final int HISTORY_LIMIT = 100;
    private static final int OUTPUT_AREA_LIMIT = 1500;
    private ConsoleModel consoleModel;
    private CodeArea inputArea = new CodeArea();
    private CodeArea outputArea = new CodeArea();
    private ObservableList<String> history = FXCollections.observableArrayList();
    private int historyIndex;
    private BooleanProperty historyStartReached = new SimpleBooleanProperty();
    private BooleanProperty historyEndReached = new SimpleBooleanProperty();
    private List<String> styleFilter;
    private final ReadOnlyBooleanWrapper edited = new ReadOnlyBooleanWrapper();

    public SplitConsolePane() {
        this(new ConsoleModel(), List.of(), List.of());
    }

    public SplitConsolePane(List<String> history, List<String> styleFilter) {
        this(new ConsoleModel(), history, styleFilter);
    }

    public SplitConsolePane(ConsoleModel consoleModel) {
        this(consoleModel, List.of(), List.of());
    }

    public SplitConsolePane(ConsoleModel consoleModel, List<String> history, List<String> styleFilter) {
        this.consoleModel = consoleModel;
        this.history.addAll(history);
        this.styleFilter = styleFilter;
        historyIndex = history.size();
        historyStartReached.set(historyIndex == 0);
        historyEndReached.set(historyIndex == history.size());
        setGraphics();
        setBehavior();
    }

    public boolean isEdited() {
        return edited.get();
    }

    public ReadOnlyBooleanProperty editedProperty() {
        return edited.getReadOnlyProperty();
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public ObservableList<String> getHistory() {
        return history;
    }

    private ObservableList<TextStyleSpans> getInput() {
        return consoleModel.getInput();
    }

    private ObservableList<TextStyleSpans> getOutput() {
        return consoleModel.getOutput();
    }

    public CodeArea getInputArea() {
        return inputArea;
    }

    public CodeArea getOutputArea() {
        return outputArea;
    }

    private void setGraphics() {
        outputArea.setEditable(false);
        outputArea.setFocusTraversable(false);

        SplitPane splitPane = new SplitPane(new VirtualizedScrollPane<>(inputArea),
                new VirtualizedScrollPane<>(outputArea));
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8f);

        setCenter(splitPane);
        // The style must be add explicitly.
        getStylesheets().add(getUserAgentStylesheet());
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("console.css").toExternalForm();
    }

    private void setBehavior() {

        inputArea.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> edited.set((Boolean) n));
        inputArea.textProperty().addListener((v, o, n) -> edited.set(true));

        inputArea.sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                inputArea.requestFocus();
            }
        });

        getOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());

                    Platform.runLater(() -> {
                        for (TextStyleSpans span : added) {
                            outputArea.appendText(span.getText());
                            int from = outputArea.getLength() - span.getStyleSpans().length();
                            outputArea.setStyleSpans(from, span.getStyleSpans());
                        }

                        int paragraphCount = outputArea.getParagraphs().size();
                        if (paragraphCount > OUTPUT_AREA_LIMIT) {
                            int lastExtraParagraph = paragraphCount - OUTPUT_AREA_LIMIT - 1;
                            outputArea.deleteText(0, 0, lastExtraParagraph,
                                    outputArea.getParagraph(lastExtraParagraph).length());
                        }

                        outputArea.moveTo(outputArea.getLength());
                        outputArea.requestFollowCaret();
                    });
                }
            }
        });
    }

    public void submit() {
        enter();

        if (inputArea.getSelectedText().isEmpty()) {
            inputArea.clear();
        } else {
            inputArea.replaceSelection("");
        }
    }

    public void eval() {
        enter();
    }

    private void enter() {
        String text = inputArea.getSelectedText();
        IndexRange selection = inputArea.getSelection();
        int from = 0;

        if (text == null || text.isEmpty()) {
            text = inputArea.getText();
        } else {
            from = selection.getStart();
        }

        // Null char may come from clipboard.
//        if (text.contains("\0")) {
//            text.replace("\0", "");
//        }

        if (!consoleModel.isReadFromPipe() && outputArea.getLength() > 0 && !outputArea.getText().endsWith("\n\n")) {
            outputArea.appendText("\n");
        }

        TextStyleSpans span = new TextStyleSpans(text + "\n", filterStyles(from, text.length()));  

        history.add(span.getText().strip());

        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }

        historyIndex = history.size();
        historyStartReached.set(historyIndex == 0);
        historyEndReached.set(historyIndex == history.size());

        getInput().add(span);
    }

    public void submit(String input) {
        if (outputArea.getLength() > 0 && !outputArea.getText().endsWith("\n\n")) {
            outputArea.appendText("\n");
        }

        getInput().add(new TextStyleSpans(input));
    }

    private StyleSpans<Collection<String>> filterStyles(int from, int length) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        inputArea.getStyleSpans(from, from + length).forEach(s -> {
            if (s.getStyle().contains("block-delimiter-match")) {
                var n = s.getStyle().stream().filter(c -> !styleFilter.contains(c)).collect(Collectors.toList());
                spansBuilder.add(n, s.getLength());
            } else {
                spansBuilder.add(s);
            }
        });

        // For extra new line
        spansBuilder.add(Collections.emptyList(), 1);
        var styleSpans = spansBuilder.create();

        return styleSpans;
    }

    public void historyUp() {

        if (historyIndex > 0 && historyIndex <= history.size()) {
            historyIndex--;
            String text = history.get(historyIndex);
            inputArea.replaceText(text);
            historyStartReached.set(historyIndex == 0);
            historyEndReached.set(historyIndex == history.size());
        }
    }

    public void historyDown() {

        if (historyIndex >= 0 && historyIndex < history.size() - 1) {
            historyIndex++;
            String text = history.get(historyIndex);
            inputArea.replaceText(text);
        } else {
            inputArea.replaceText("");
            historyIndex = history.size();
        }

        historyStartReached.set(historyIndex == 0);
        historyEndReached.set(historyIndex == history.size());

    }

    public ReadOnlyBooleanProperty historyStartReachedProperty() {
        return historyStartReached;
    }

    public ReadOnlyBooleanProperty historyEndReachedProperty() {
        return historyEndReached;
    }

    public void dispose() {
        inputArea.dispose();
        outputArea.dispose();
    }
}
