package dev.jshfx.base.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.FileManager;
import dev.jshfx.base.sys.TaskManager;
import dev.jshfx.j.nio.file.XFiles;
import dev.jshfx.jfx.concurrent.CTask;
import dev.jshfx.jfx.scene.control.AlertBuilder;
import dev.jshfx.jfx.scene.control.ButtonTypes;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;

public class ActionController {

    private static FindDialog findDialog;
    private RootPane rootPane;
    private Optional<ButtonType> result = Optional.of(ButtonType.NO);

    public ActionController(RootPane rootPane) {
        this.rootPane = rootPane;
    }

    public void close(Event event) {
        if (event.getSource()instanceof Tab tab) {
            close(tab, event);
        } else if (event.getSource()instanceof MenuItem item) {
            Tab tab = (Tab) item.getParentPopup().getUserData();
            close(tab, null);
        }
    }

    public void close(Tab tab) {
        close(tab, null);
    }

    public void closeOthers(ActionEvent event) {
        if (event.getSource()instanceof MenuItem item) {
            Tab tab = (Tab) item.getParentPopup().getUserData();
            var tabs = rootPane.getTabs().stream().filter(t -> t != tab).collect(Collectors.toList());

            close(tabs, null);
        }
    }

    public void closeAll() {
        close(new ArrayList<>(rootPane.getTabs()), null);
    }

    public void closeApp(Event event) {
        close(new ArrayList<>(rootPane.getTabs()), event);
    }

    private void close(Tab tab, Event event) {
        result = Optional.of(ButtonType.NO);
        close(tab, event, List.of());
    }

    private void close(List<Tab> tabs, Event event) {
        result = Optional.of(ButtonType.NO);
        List<ButtonType> buttons = List.of();

        if (tabs.stream().filter(t -> ((ContentPane) t.getContent()).isModified()).limit(2).toList().size() == 2) {
            buttons = List.of(ButtonTypes.YES_ALL, ButtonTypes.NO_ALL);
        }

        for (Tab tab : tabs) {
            close(tab, event, buttons);
            if (result.get() == ButtonType.CANCEL) {
                break;
            }
        }
    }

    private void close(Tab tab, Event event, List<ButtonType> additionalButtons) {

        ContentPane contentPane = (ContentPane) tab.getContent();

        if (contentPane.isModified() && result.get() != ButtonTypes.YES_ALL && result.get() != ButtonTypes.NO_ALL) {

            var buttons = new ArrayList<>(List.of(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL));
            buttons.addAll(additionalButtons);
            result = AlertBuilder.get(AlertType.CONFIRMATION).initOwner(rootPane.getScene().getWindow())
                    .contentTextKey("msg.file.save", contentPane.getFXPath().getPath().toString()).buttonTypes(buttons)
                    .build().showAndWait();

        }

        Runnable close = () -> {
            contentPane.dispose();

            if (event == null || !(event.getSource() instanceof Tab)) {
                rootPane.getTabs().remove(tab);
            }
        };

        result.ifPresent(response -> {
            if (response == ButtonType.YES || response == ButtonTypes.YES_ALL) {
                save(contentPane, close, () -> {
                    if (event != null) {
                        event.consume();
                    }
                });
            } else if (response == ButtonType.CANCEL) {
                if (event != null) {
                    event.consume();
                }
            } else {
                close.run();
            }
        });
    }

    public void newShell() {

        String name = XFiles.getUniqueName(n -> rootPane.exists(n), FXResourceBundle.getBundle().getString​("new"));

        var shellPane = create(Path.of(XFiles.appendFileExtension(name, FileManager.JSH)));
        rootPane.addSelect(shellPane);
    }

    public void openFile() {
        List<Path> files = FileDialogUtils.openTextFiles(rootPane.getScene().getWindow());

        if (!files.isEmpty()) {
            List<Path> newFiles = rootPane.getNew(files);
            var task = new Task<List<ContentPane>>() {

                int i = 0;

                @Override
                protected List<ContentPane> call() throws Exception {
                    List<ContentPane> panes = new ArrayList<>();

                    newFiles.forEach(p -> {
                        var contentPane = create(p);
                        panes.add(contentPane);
                        updateProgress(i++, newFiles.size());
                    });

                    return panes;
                }
            };

            WindowUtils.showProgress(rootPane.getScene().getWindow(), task);

            task.setOnSucceeded(e -> rootPane.add(task.getValue()));

            TaskManager.get().execute(task);
        }
    }

    private ContentPane create(Path path) {
        ContentPane pane = null;
        String input = "";

        try {
            if (path.isAbsolute()) {
                input = Files.readString(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (FileManager.SHELL_EXTENSIONS.contains(XFiles.getFileExtension(path))) {

            var shellPane = new ShellPane(path, input);
            shellPane.setActions(rootPane.getActions());
            pane = shellPane;

        } else {
            var editorPane = new EditorPane(path, input);
            editorPane.setActions(rootPane.getActions());
            pane = editorPane;
        }

        return pane;
    }

    public void saveFile() {
        save(rootPane.getContentPane());
    }

    public void saveAsFile() {
        var contentPane = rootPane.getContentPane();
        String output = contentPane.getContent();
        Path fileName = contentPane.getFXPath().getPath().getFileName();

        var path = FileDialogUtils.saveSourceJavaFile(contentPane.getScene().getWindow(), fileName);

        path.ifPresent(savePath -> TaskManager.get().executeSequentially(
                CTask.create(() -> Files.writeString(savePath, output)).onSucceeded(p -> contentPane.saved(p))));
    }

    public void save(ContentPane contentPane) {
        save(contentPane, () -> {
        }, () -> {
        });
    }

    public void save(ContentPane pane, Runnable onSucceded, Runnable onFailed) {
        String output = pane.getContent();
        Path path = pane.getFXPath().getPath();

        if (!path.isAbsolute()) {
            path = FileDialogUtils.saveSourceJavaFile(pane.getScene().getWindow(), path.getFileName()).orElse(null);
        }

        if (path != null) {
            var savePath = path;
            TaskManager.get()
                    .executeSequentially(CTask.create(() -> Files.writeString(savePath, output)).onSucceeded(p -> {
                        pane.saved(p);
                        onSucceded.run();
                    }).onFailed(p -> onFailed.run()));
        } else {
            onFailed.run();
        }
    }

    public void saveAll() {

        var modifiedPanes = rootPane.getModified();
        modifiedPanes.forEach(cp -> save(cp));

    }

    public void showFindDialog() {
        if (findDialog == null) {
            findDialog = new FindDialog(rootPane.getScene().getWindow(), rootPane.envPaneProperty());
            findDialog.show();
            findDialog.setOnCloseRequest(e -> {
                findDialog.store();
                findDialog = null;
            });
        } else {
            findDialog.setSelection();
        }
    }

    public void closeFindDialog() {

        if (findDialog != null) {
            findDialog.close();
        }
    }
}
