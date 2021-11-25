package dev.jshfx.base.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jshfx.base.jshell.Completion;
import dev.jshfx.base.jshell.JShellUtils;
import dev.jshfx.base.jshell.Session;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.fxmisc.richtext.CodeAreaWrappers;
import dev.jshfx.fxmisc.richtext.CommentWrapper;
import dev.jshfx.fxmisc.richtext.CompletionPopup;
import dev.jshfx.fxmisc.richtext.StyleClassedTextAreaWrapper;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jfx.concurrent.CTask;
import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jfx.scene.control.SplitConsolePane;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public class ShellPane extends PathPane {

    private SplitConsolePane consolePane;
    private Completion completion;
    private Session session;
    private TaskQueuer taskQueuer = new TaskQueuer();
    private Finder finder;

    public ShellPane(String name) {
        this(Path.of(name), "");
    }

    public ShellPane(Path path, String input) {
        super(path);

        List<String> history = JsonUtils.get().fromJson(FileManager.HISTORY_FILE, List.class, List.of());
        consolePane = new SplitConsolePane(history, List.of("block-delimiter-match"));
        getProperties().put(getClass(), consolePane.getInputArea());
        session = new Session(consolePane, taskQueuer);
        session.setOnExitCommand(
                () -> Platform.runLater(() -> onCloseRequest.handle(new Event(this, this, Event.ANY))));
        session.setOnResult(this::handleResult);
        completion = new Completion(consolePane.getInputArea(), session);

        getChildren().add(consolePane);

        consolePane.getInputArea().setParagraphGraphicFactory(LineNumberFactory.get(consolePane.getInputArea()));

        CodeAreaWrappers.get(consolePane.getInputArea(), "java").style()
                .highlighting(consolePane.getConsoleModel().getReadFromPipe()).indentation();

        CodeAreaWrappers.get(consolePane.getOutputArea(), "java").style();

        consolePane.getInputArea().replaceText(input);
        setBehavior();
        consolePane.forgetEdit();
        finder = new FinderImpl(consolePane.getInputArea());
    }

    @Override
    public void setActions(Actions actions) {
        super.setActions(actions);
        actions.setActions(this);
    }

    @Override
    public void bind(Actions actions) {
        super.bind(actions);
        actions.bind(this);
    }

    @Override
    public void saved(Path path) {
        super.saved(path);
        consolePane.forgetEdit();
    }

    @Override
    public String getSelection() {
        return consolePane.getInputArea().getSelectedText();
    }

    @Override
    public Finder getFinder() {
        return finder;
    }

    private void handleResult(SnippetEvent event, Object obj) {

        if (event.snippet().subKind() == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
            Platform.runLater(() -> DialogUtils.show(getScene().getWindow(), obj));
        }
    }

    private void setBehavior() {

        modified.bind(consolePane.editedProperty());
        consolePane.getOutputHeader().textProperty().bind(session.getTimer().textProperty());

        sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                session.setIO();
            }
        });

        consolePane.getInputArea().caretPositionProperty().addListener((v, o, n) -> {
            if (CompletionPopup.get().isShowing()) {
                showCodeCompletion();
            }
        });

        consolePane.getConsoleModel().getInputToOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
                    for (TextStyleSpans span : added) {
                        session.process(span.getText());
                    }
                }
            }
        });

        consolePane.getHistory().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    List<? extends String> history = new ArrayList<>(consolePane.getHistory());
                    JsonUtils.get().toJson(history, FileManager.HISTORY_FILE);
                }
            }
        });
    }

    @Override
    public String getContent() {
        return getConsolePane().getInputArea().getText();
    }

    public void showCodeCompletion() {

        Optional<Bounds> boundsOption = consolePane.getInputArea().getCaretBounds();
        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            CompletionPopup.get().setDocumentation(completion.getCompletor()::loadDocumentation);
            CompletionPopup.get().setCompletionItem(completion.getCompletor()::getCompletionItem);
            CompletionPopup.get().clear();
            CompletionPopup.get().show(consolePane.getInputArea(), bounds.getMaxX(), bounds.getMaxY());
            CTask<Void> task = CTask
                    .create(() -> completion.getCompletor().getCompletionItems(i -> CompletionPopup.get().add(i)));
            taskQueuer.add(Session.PRIVILEDGED_TASK_QUEUE, task);
        }
    }

    public void toggleComment() {
        new CommentWrapper<>(consolePane.getInputArea()).toggleComment();
    }

    public void showHistorySearch() {
        DialogUtils.showHistorySearch(getScene().getWindow(), consolePane.getHistory(),
                s -> consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(), s));
    }

    public void eval() {
        String text = consolePane.getInputArea().getSelectedText();

        if (text == null || text.isEmpty()) {
            text = consolePane.getInputArea().getText();
        }

        eval(text);
    }

    public void evalLine() {
        String text = JShellUtils.getCurrentLineSpan(consolePane.getInputArea()).originalText();
        eval(text);
    }

    private void eval(String text) {
        if (consolePane.getOutputArea().getLength() > 0 && !consolePane.getOutputArea().getText().endsWith("\n")) {
            consolePane.getOutputArea().appendText("\n");
        }

        session.process(text);
    }

    public void submit() {
        String text = consolePane.getInputArea().getSelectedText();
        IndexRange selection = consolePane.getInputArea().getSelection();
        int from = 0;

        if (text == null || text.isEmpty()) {
            text = consolePane.getInputArea().getText();
        } else {
            from = selection.getStart();
        }

        consolePane.submit(from, text);

        if (consolePane.getInputArea().getSelectedText().isEmpty()) {
            consolePane.getInputArea().clear();
        } else {
            consolePane.getInputArea().replaceSelection("");
        }
    }

    public void submitLine() {
        var lineSpan = JShellUtils.getCurrentLineSpan(consolePane.getInputArea());
        int from = consolePane.getInputArea().getAbsolutePosition(lineSpan.firstParagraphIndex(), 0);

        consolePane.submit(from, lineSpan.originalText());

        consolePane.getInputArea().deleteText(lineSpan.firstParagraphIndex(), 0, lineSpan.lastParagraphIndex(),
                consolePane.getInputArea().getParagraphLength(lineSpan.lastParagraphIndex()));
    }

    public Session getSession() {
        return session;
    }

    public SplitConsolePane getConsolePane() {
        return consolePane;
    }

    public void insertDirPath() {
        var dir = FileDialogUtils.getDirectory(getScene().getWindow());
        var joining = getJoining();

        dir.ifPresent(d -> {
            consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(),
                    joining.prefix() + d.toString() + joining.delimiter());
        });
    }

    public void insertFilePaths() {
        var files = FileDialogUtils.openJavaFiles(getScene().getWindow());
        var joining = getJoining();

        files.forEach(f -> {
            consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(),
                    joining.prefix() + f.toString() + joining.delimiter());
        });
    }

    record Joining(String prefix, String delimiter) {
    }

    private Joining getJoining() {
        int end = consolePane.getInputArea().getCaretPosition();
        int start = end > 0 ? end - 1 : 0;
        String caretChar = consolePane.getInputArea().getText(start, end);
        String delimiter = caretChar.matches("\\s|^$") ? " " : "";
        String prefix = delimiter.isEmpty() ? File.pathSeparator : "";

        return new Joining(prefix, delimiter);
    }

    public void insertSaveFilePath() {
        var file = FileDialogUtils.saveSourceJavaFile(getScene().getWindow());

        file.ifPresent(f -> {
            consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(), f.toString() + " ");
        });
    }

    @Override
    public void init() {
        session.doImports(consolePane.getInputArea().getText());
    }

    @Override
    public void activate() {
        session.setIO();
    }

    @Override
    public void dispose() {
        super.dispose();
        session.close();
        consolePane.dispose();
        taskQueuer.clear();
    }

    private static class FinderImpl implements Finder {

        private static final String FIND_STYLE = "jsh-find";

        private StyleClassedTextAreaWrapper areaWrapper;
        private boolean inSelection;
        private List<Integer> selectionParagraphs = List.of();

        public FinderImpl(CodeArea area) {
            this.areaWrapper = new StyleClassedTextAreaWrapper(area);

            area.focusedProperty().addListener((v, o, n) -> {
                if (n) {
                    selectionParagraphs.forEach(i -> areaWrapper.getArea().clearParagraphStyle(i));
                    selectionParagraphs = List.of();
                    inSelection = false;
                }
            });
        }

        @Override
        public void setScope(boolean inSelection) {
            this.inSelection = inSelection;
            if (inSelection) {
                if (selectionParagraphs.isEmpty()) {
                    var selectionRange = areaWrapper.getArea().getSelection();
                    if (selectionRange.getLength() == 0) {
                        int currentParagraph = areaWrapper.getArea().getCurrentParagraph();
                        int start = areaWrapper.getArea().getAbsolutePosition(currentParagraph, 0);
                        int end = areaWrapper.getArea().getAbsolutePosition(currentParagraph,
                                areaWrapper.getArea().getParagraphLength(currentParagraph));
                        selectionRange = new IndexRange(start, end);
                    }
                    selectionParagraphs = areaWrapper.getSelectedParagraphs(selectionRange);
                    areaWrapper.getArea().deselect();
                }

                selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
                areaWrapper.getArea().moveTo(selectionParagraphs.get(0), 0);
            } else {
                selectionParagraphs.forEach(i -> areaWrapper.getArea().clearParagraphStyle(i));
            }
        }

        private int getOffset() {
            return inSelection ? areaWrapper.getArea().getAbsolutePosition(selectionParagraphs.get(0), 0) : 0;
        }

        private int getPosition() {
            return inSelection ? areaWrapper.getArea().getCaretPosition() - getOffset()
                    : areaWrapper.getArea().getCaretPosition();
        }

        private String getInput() {
            return inSelection ? selectionParagraphs.stream().map(i -> areaWrapper.getArea().getParagraph(i).getText())
                    .collect(Collectors.joining("\n")) : areaWrapper.getArea().getText();
        }

        @Override
        public void findPrevious(Pattern pattern) {

            Matcher matcher = pattern.matcher(getInput());
            int start = -1;
            int end = 0;

            var selection = areaWrapper.getArea().getSelection();

            if (selection.getLength() == 0) {
                selection = new IndexRange(areaWrapper.getArea().getCaretPosition(),
                        areaWrapper.getArea().getCaretPosition());
            }

            int offset = getOffset();

            while (matcher.find() && matcher.start() < selection.getStart() - offset) {
                start = matcher.start();
                end = matcher.end();
            }

            if (start > -1) {
                areaWrapper.getArea().selectRange(start + offset, end + offset);
            } else {
                while (matcher.find(getPosition())) {
                    start = matcher.start();
                    end = matcher.end();
                    areaWrapper.getArea().moveTo(end + offset);
                }

                if (start > -1) {
                    areaWrapper.getArea().selectRange(start + offset, end + offset);
                }
            }
        }

        @Override
        public void findNext(Pattern pattern) {

            Matcher matcher = pattern.matcher(getInput());
            int offset = getOffset();

            if (matcher.find(getPosition())) {
                areaWrapper.getArea().selectRange(matcher.start() + offset, matcher.end() + offset);
            } else {
                if (matcher.find(0)) {
                    areaWrapper.getArea().selectRange(matcher.start() + offset, matcher.end() + offset);
                }
            }
        }

        @Override
        public void replacePrevious(Pattern pattern, String replacement) {
            String selection = areaWrapper.getArea().getSelectedText();

            if (selection.matches(pattern.pattern())) {
                areaWrapper.getArea().replaceSelection(replacement);
                if (inSelection) {
                    selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
                }
            }

            findPrevious(pattern);

        }

        @Override
        public void replaceNext(Pattern pattern, String replacement) {
            String selection = areaWrapper.getArea().getSelectedText();

            if (selection.matches(pattern.pattern())) {
                areaWrapper.getArea().replaceSelection(replacement);
                if (inSelection) {
                    selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
                }
            }

            findNext(pattern);
        }

        @Override
        public void replaceAll(Pattern pattern, String replacement) {
            Matcher matcher = pattern.matcher(getInput());

            var result = matcher.replaceAll(replacement);

            if (inSelection) {
                int lastParagraph = selectionParagraphs.get(selectionParagraphs.size() - 1);
                areaWrapper.getArea().replaceText(selectionParagraphs.get(0), 0, lastParagraph,
                        areaWrapper.getArea().getParagraphLength(lastParagraph), result);
                selectionParagraphs.forEach(i -> areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
            } else {
                areaWrapper.getArea().replaceText(result);
            }
        }
    }
}
