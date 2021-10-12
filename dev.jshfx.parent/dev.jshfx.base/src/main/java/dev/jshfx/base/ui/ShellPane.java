package dev.jshfx.base.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.fxmisc.richtext.LineNumberFactory;

import dev.jshfx.base.jshell.Completion;
import dev.jshfx.base.jshell.Session;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.fxmisc.richtext.CodeAreaWrappers;
import dev.jshfx.fxmisc.richtext.CompletionItem;
import dev.jshfx.fxmisc.richtext.CompletionPopup;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jfx.concurrent.CTask;
import dev.jshfx.jfx.concurrent.TaskQueuer;
import dev.jshfx.jfx.file.FXPath;
import dev.jshfx.jfx.scene.control.SplitConsolePane;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Bounds;

public class ShellPane extends Part {

    private SplitConsolePane consolePane;
    private Completion completion;
    private Session session;
    private TaskQueuer taskQueuer = new TaskQueuer();
    private FXPath path;

    public ShellPane() {
        this(Path.of("new.jsh"));
    }

    public ShellPane(Path path) {
        this.path = new FXPath(path);

        List<String> history = JsonUtils.get().fromJson(FileManager.HISTORY_FILE, List.class, List.of());
        consolePane = new SplitConsolePane(history, List.of("block-delimiter-match"));
        getProperties().put(getClass(), consolePane.getInputArea());
        session = new Session(consolePane, taskQueuer);
        completion = new Completion(session);

        getChildren().add(consolePane);

        consolePane.getInputArea().setParagraphGraphicFactory(LineNumberFactory.get(consolePane.getInputArea()));
        Actions.get().setEditContextMenu(consolePane.getInputArea());
        Actions.get().setReadOnlyContextMenu(consolePane.getOutputArea());

        CodeAreaWrappers.get(consolePane.getInputArea(), "java").style()
                .highlighting(consolePane.getConsoleModel().getReadFromPipe()).indentation();

        CodeAreaWrappers.get(consolePane.getOutputArea(), "java").style();

        setBehavior();
    }

    private void setBehavior() {

        title.bind(
                Bindings.createStringBinding(() -> createTitle(), path.nameProperty(), consolePane.editedProperty()));
        longTitle.bind(Bindings.createStringBinding(() -> path.getPath().toString(), path.pathProperty()));

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

    private String createTitle() {
        String result = consolePane.isEdited() ? "*" + path.getName() : path.getName();

        return result;
    }

    public void showCodeCompletion() {
        CTask<Collection<CompletionItem>> task = CTask
                .create(() -> completion.getCompletionItems(consolePane.getInputArea()))
                .onSucceeded(this::codeCompletion);

        taskQueuer.add(Session.PRIVILEDGED_TASK_QUEUE, task);
    }

    private void codeCompletion(Collection<CompletionItem> items) {

        Optional<Bounds> boundsOption = consolePane.getInputArea().caretBoundsProperty().getValue();
        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            CompletionPopup.get().setDocumentation(completion::loadDocumentation);
            CompletionPopup.get().setItems(items);
            CompletionPopup.get().show(consolePane.getInputArea(), bounds.getMaxX(), bounds.getMaxY());
        }
    }

    public Session getSession() {
        return session;
    }

    public SplitConsolePane getConsolePane() {
        return consolePane;
    }

    public ReadOnlyBooleanProperty closedProperty() {
        return session.closedProperty();
    }

    public void insertDirPath() {
        var dir = FileDialogUtils.getDirectory(getScene().getWindow());

        dir.ifPresent(d -> {
            consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(), d.toString() + " ");
        });
    }

    public void insertFilePaths() {
        var files = FileDialogUtils.getJavaFiles(getScene().getWindow());

        files.forEach(f -> {
            consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(), f.toString() + " ");
        });
    }

    public void insertSaveFilePaths() {
        var file = FileDialogUtils.saveJavaFile(getScene().getWindow());

        file.ifPresent(f -> {
            consolePane.getInputArea().insertText(consolePane.getInputArea().getCaretPosition(), f.toString() + " ");
        });
    }

    public void dispose() {
        var task = CTask.create(() -> session.close()).onFinished(t -> consolePane.dispose());

        taskQueuer.add(Session.PRIVILEDGED_TASK_QUEUE, task);
    }
}
