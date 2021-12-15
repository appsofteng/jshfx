package dev.jshfx.base.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jshfx.base.jshell.Completion;
import dev.jshfx.base.jshell.Session;
import dev.jshfx.base.jshell.commands.ResolveCommand;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.fxmisc.richtext.CodeAreaWrappers;
import dev.jshfx.fxmisc.richtext.CommentWrapper;
import dev.jshfx.fxmisc.richtext.CompletionPopup;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jfx.concurrent.QueueTask;
import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jx.tools.GroupNames;
import dev.jshfx.jx.tools.Lexer;
import dev.jshfx.jx.tools.Token;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;

public class ShellPane extends AreaPane {

    private static final int HISTORY_LIMIT = 100;

    private ObservableList<String> history = FXCollections.observableArrayList();
    private int historyIndex;
    private BooleanProperty historyStartReached = new SimpleBooleanProperty();
    private BooleanProperty historyEndReached = new SimpleBooleanProperty();
    private List<String> styleFilter = List.of("block-delimiter-match");
    private Completion completion;
    private Session session;
    private TaskQueuer taskQueuer = new TaskQueuer();
    private boolean completionContains;
    private Lexer lexer;

    public ShellPane(String name) {
        this(Path.of(name), "");
    }

    public ShellPane(Path path, String input) {
        super(path, input);

        history.addAll(JsonUtils.get().fromJson(FileManager.HISTORY_FILE, List.class, List.of()));
        session = new Session(consoleModel, taskQueuer);
        session.setOnExitCommand(
                () -> Platform.runLater(() -> onCloseRequest.handle(new Event(this, this, Event.ANY))));
        completion = new Completion(getArea(), session, lexer);

        getChildren().add(new VirtualizedScrollPane<>(getArea()));

        setBehavior();
    }

    @Override
    public void setActions(Actions actions) {
        super.setActions(actions);

        actions.setShellContextMenu(getArea());
        actions.addShellKeyHandlers(getArea());

        handlers.put(actions.getSubmitAction(), () -> submit());
        handlers.put(actions.getSubmitLineAction(), () -> submitLine());
        handlers.put(actions.getEvalAction(), () -> eval());
        handlers.put(actions.getEvalLineAction(), () -> evalLine());
        handlers.put(actions.getHistoryUpAction(), () -> historyUp());
        handlers.put(actions.getHistoryDownAction(), () -> historyDown());
        handlers.put(actions.getHistorySearchAction(), () -> showHistorySearch());
        handlers.put(actions.getInsertDirPathAction(), () -> insertDirPath());
        handlers.put(actions.getInsertFilePathAction(), () -> insertFilePaths());
        handlers.put(actions.getInsertSeparatedFilePathAction(), () -> insertFilePaths(File.pathSeparator));
        handlers.put(actions.getInsertSaveFilePathAction(), () -> insertSaveFilePath());
        handlers.put(actions.getCodeCompletionAction(), () -> showCodeCompletion(false));
        handlers.put(actions.getCodeCompletionContainsAction(), () -> showCodeCompletion(true));
        handlers.put(actions.getToggleCommentAction(), () -> toggleComment());
    }

    @Override
    public void bindActions(Actions actions) {
        super.bindActions(actions);

        actions.getEvalAction().setDisabled(false);
        actions.getEvalLineAction().setDisabled(false);
        actions.getSubmitAction().setDisabled(false);
        actions.getSubmitLineAction().setDisabled(false);

        actions.getHistoryUpAction().disabledProperty().bind(historyStartReachedProperty());
        actions.getHistoryDownAction().disabledProperty().bind(historyEndReachedProperty());
    }

    @Override
    protected void wrap(CodeArea area) {
        lexer = CodeAreaWrappers.get(area, "java").style().highlighting(consoleModel.getReadFromPipe()).indentation()
                .find().getLexer();
    }

    private void setBehavior() {

        consoleHeaderText.bind(session.getTimer().textProperty());

        sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                session.setIO();
            }
        });

        getArea().caretPositionProperty().addListener((v, o, n) -> {
            if (CompletionPopup.get().isShowing()) {
                if (getArea().getLength() == lexer.getLength()) {
                    showCodeCompletion(completionContains);
                }
            }
        });

        lexer.lengthProperty().addListener((v, o, n) -> {
            if (CompletionPopup.get().isShowing()) {
                showCodeCompletion(completionContains);
            }
        });

        history.addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    List<? extends String> h = new ArrayList<>(history);
                    JsonUtils.get().toJson(h, FileManager.HISTORY_FILE);
                }
            }
        });
    }

    public void showCodeCompletion(boolean contains) {
        completionContains = contains;

        Optional<Bounds> boundsOption = getArea().getCaretBounds();
        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            var completor = completion.getCompletor();
            CompletionPopup.get().setDocumentation(completor::loadDocumentation);
            CompletionPopup.get().setCompletionItem(completor::getCompletionItem);
            CompletionPopup.get().clear();
            CompletionPopup.get().show(getArea(), bounds.getMaxX(), bounds.getMaxY());
            QueueTask<Void> task = QueueTask
                    .create(() -> completor.getCompletionItems(contains, i -> CompletionPopup.get().add(i)))
                    .queueId(Session.PRIVILEDGED_TASK_QUEUE);
            taskQueuer.add(task);
        }
    }

    public void toggleComment() {
        new CommentWrapper<>(getArea()).toggleComment();
    }

    public void showHistorySearch() {
        WindowUtils.showHistorySearch(getScene().getWindow(), history,
                s -> getArea().insertText(getArea().getCaretPosition(), s));
    }

    public void eval() {
        String text = getArea().getSelectedText();

        if (text == null || text.isEmpty()) {
            text = getArea().getText();
        }

        eval(text);
    }

    public void evalLine() {
        String text = getArea().getParagraph(getArea().getCurrentParagraph()).getText();
        eval(text);
    }

    private void eval(String text) {
        session.process(text);
    }

    public void submit() {
        String text = getArea().getSelectedText();
        IndexRange selection = getArea().getSelection();
        int from = 0;

        if (text == null || text.isEmpty()) {
            text = getArea().getText();
        } else {
            from = selection.getStart();
        }

        submit(from, text);

        if (getArea().getSelectedText().isEmpty()) {
            getArea().clear();
        } else {
            getArea().replaceSelection("");
        }
    }

    public void submitLine() {
        String text = getArea().getParagraph(getArea().getCurrentParagraph()).getText();
        int from = getArea().getAbsolutePosition(getArea().getCurrentParagraph(), 0);

        submit(from, text);

        getArea().deleteText(getArea().getCurrentParagraph(), 0, getArea().getCurrentParagraph(),
                getArea().getParagraphLength(getArea().getCurrentParagraph()));
    }

    private void submit(int from, String text) {

        // Null char may come from clipboard.
//        if (text.contains("\0")) {
//            text.replace("\0", "");
//        }

        TextStyleSpans span = new TextStyleSpans(text + "\n", filterStyles(from, text.length()));

        history.add(text);

        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }

        historyIndex = history.size();
        historyStartReached.set(historyIndex == 0);
        historyEndReached.set(historyIndex == history.size());

        if (consoleModel.addInput(span)) {
            session.process(text);
        }
    }

    private StyleSpans<Collection<String>> filterStyles(int from, int length) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        getArea().getStyleSpans(from, from + length).forEach(s -> {
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

    public Session getSession() {
        return session;
    }

    public void historyUp() {

        if (historyIndex > 0 && historyIndex <= history.size()) {
            historyIndex--;
            String text = history.get(historyIndex);
            // Ensures highlighting when the next text is the same as the previous.
            getArea().replaceText("");
            getArea().replaceText(text);
            historyStartReached.set(historyIndex == 0);
            historyEndReached.set(historyIndex == history.size());
        }
    }

    public void historyDown() {

        if (historyIndex >= 0 && historyIndex < history.size() - 1) {
            historyIndex++;
            String text = history.get(historyIndex);
            // Ensures highlighting when the next text is the same as the previous.
            getArea().replaceText("");
            getArea().replaceText(text);
        } else {
            getArea().replaceText("");
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

    @Override
    public void init() {

        session.init();

        var resolveCommands = lexer.getTokens().stream().filter(
                t -> t.getType().equals(GroupNames.JSHELLCOMMAND) && t.getValue().startsWith(ResolveCommand.NAME))
                .map(Token::getValue).collect(Collectors.joining("\n"));

        session.process(resolveCommands);

        String imports = getArea().getText().lines().map(line -> line.trim()).filter(line -> line.startsWith("import"))
                .collect(Collectors.joining("\n"));

        session.process(imports);
    }

    @Override
    public void activate() {
        super.activate();
        session.setIO();
    }

    @Override
    public void dispose() {
        super.dispose();
        session.close();
        taskQueuer.clear();
    }
}
