package dev.jshfx.base.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Optional<Bounds> boundsOption = consolePane.getInputArea().caretBoundsProperty().getValue();
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

        text = JShellUtils.joinCommandLines(text);

        eval(text);
    }

    public void evalLine() {
        String text = JShellUtils.getCurrentLineSpan(consolePane.getInputArea()).text();
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

        String original = text;

        text = JShellUtils.joinCommandLines(text);
        consolePane.submit(from, text, original);

        if (consolePane.getInputArea().getSelectedText().isEmpty()) {
            consolePane.getInputArea().clear();
        } else {
            consolePane.getInputArea().replaceSelection("");
        }
    }

    public void submitLine() {
        var lineSpan = JShellUtils.getCurrentLineSpan(consolePane.getInputArea());
        int from = consolePane.getInputArea().getAbsolutePosition(lineSpan.firstParagraphIndex(), 0);

        consolePane.submit(from, lineSpan.text(), lineSpan.originalText());

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
        var files = FileDialogUtils.getJavaFiles(getScene().getWindow());
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
        session.getSnippetProcessor().doImports(consolePane.getInputArea().getText());
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
        private IndexRange selectionRange = new IndexRange(0, 0);
        private String selectedText = null;
        private int position;
        private String input = "";

        public FinderImpl(CodeArea area) {
            this.areaWrapper = new StyleClassedTextAreaWrapper(area);

            area.focusedProperty().addListener((v, o, n) -> {
                if (n) {
                    areaWrapper.getSelectedParagraphs(selectionRange).forEach(i ->  areaWrapper.getArea().clearParagraphStyle(i));
                    selectionRange = new IndexRange(0, 0);
                    selectedText = null;     
                    position = 0;
                    input = "";
                }
            });
        }

        @Override
        public void findPrevious(Pattern pattern, boolean inSelection) {
            update(inSelection);

            Matcher matcher = pattern.matcher(input);
            int start = -1;
            int end = 0;
            
            while (matcher.find() && matcher.end() < position) {
                start = matcher.start();
                end = matcher.end();
            }
            
            if (start > -1) {
                areaWrapper.getArea().selectRange(start + selectionRange.getStart(),
                        end + selectionRange.getStart());
                position = end;
            } else {
                while (matcher.find(position)) {
                    start = matcher.start();
                    end = matcher.end();
                    position = end;
                }
                
                if (start > -1) {
                    areaWrapper.getArea().selectRange(start + selectionRange.getStart(),
                            end + selectionRange.getStart());
                }
            }
        }

        @Override
        public void findNext(Pattern pattern, boolean inSelection) {
            update(inSelection);

            Matcher matcher = pattern.matcher(input);

            if (matcher.find(position)) {
                areaWrapper.getArea().selectRange(matcher.start() + selectionRange.getStart(),
                        matcher.end() + selectionRange.getStart());
                position = matcher.end();
            } else {
                position = 0;
                if (matcher.find(position)) {
                    areaWrapper.getArea().selectRange(matcher.start() + selectionRange.getStart(),
                            matcher.end() + selectionRange.getStart());
                    position = matcher.end();
                }
            }
        }
        
        private void update(boolean inSelection) {
            if (inSelection) {
                if (selectedText == null) {
                    input = areaWrapper.getArea().getSelectedText();
                    selectionRange = areaWrapper.getArea().getSelection();
                    selectedText = input;
                    areaWrapper.getSelectedParagraphs(selectionRange).forEach(i ->  areaWrapper.getArea().setParagraphStyle(i, List.of(FIND_STYLE)));
                    areaWrapper.getArea().deselect();
                } else {
                    input = selectedText;
                }
            } else {
                input = areaWrapper.getArea().getText();
                position = areaWrapper.getArea().getCaretPosition();
            }
        }

        @Override
        public void replacePrevious(Pattern pattern, boolean inSelection) {
            // TODO Auto-generated method stub

        }

        @Override
        public void replaceNext(Pattern pattern, boolean inSelection) {
            // TODO Auto-generated method stub

        }

        @Override
        public void replaceAll(Pattern pattern, boolean inSelection) {
            // TODO Auto-generated method stub

        }
    }
}
