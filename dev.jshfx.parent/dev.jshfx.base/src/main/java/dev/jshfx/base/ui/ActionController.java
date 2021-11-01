package dev.jshfx.base.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;

public class ActionController {

    private RootPane rootPane;
    private Optional<ButtonType> result;

    public ActionController(RootPane rootPane) {
        this.rootPane = rootPane;
    }

    public void close(Event event) {
        result = Optional.of(ButtonType.NO);
        if (event.getSource()instanceof Tab tab) {
            close(tab, event);
        } else if (event.getSource()instanceof MenuItem item) {
            Tab tab = (Tab) item.getParentPopup().getUserData();
            close(tab);
        }
    }

    public void close(Tab tab) {
        result = Optional.of(ButtonType.NO);
        close(tab, null);
    }

    public void closeOtherTabs(ActionEvent event) {
        result = Optional.of(ButtonType.NO);
        if (event.getSource()instanceof MenuItem item) {
            Tab tab = (Tab) item.getParentPopup().getUserData();
            rootPane.getTabs().filtered(t -> t != tab)
            .forEach(t -> close(t, event));
        }
    }

    public void closeAllTabs(Event event) {
        result = Optional.of(ButtonType.NO);
        rootPane.getTabs().forEach(t -> close(t, event));
    }

    private void close(Tab tab, Event event) {

        ContentPane contentPane = (ContentPane) tab.getContent();

        if (contentPane.isModified() && result.get() != ButtonTypes.YES_ALL && result.get() != ButtonTypes.NO_ALL) {

            result = AlertBuilder.get(AlertType.CONFIRMATION).initOwner(rootPane.getScene().getWindow())
                    .contentTextKey("msg.file.save", contentPane.getFXPath().getPath().toString())
                    .buttonTypes(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL).build().showAndWait();

        }

        Runnable close = () -> {
            contentPane.dispose();

            if (event == null) {
                // rootPane.getTabs().remove(tab);
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

        String name = FXResourceBundle.getBundle().getString​("new");
        int i = 0;
        while (rootPane.exists(name + i)) {
            i++;
        }

        var shellPane = new ShellPane(name + i);
        shellPane.setActions(rootPane.getActions());
        rootPane.addSelect(shellPane);
    }

    public void openFile() {
        List<Path> files = FileDialogUtils.getJavaFiles(rootPane.getScene().getWindow());
        List<Path> newFiles = rootPane.getNew(files);
        if (!files.isEmpty()) {
            TaskManager.get().execute(
                    CTask.create(() -> create(newFiles)).onSucceeded(contentPanes -> rootPane.add(contentPanes)));
        }
    }

    private List<ContentPane> create(List<Path> paths) {

        var panes = paths.stream().filter(p -> FileManager.EXTENSIONS.contains(XFiles.getFileExtension(p)))
                .map(this::create).collect(Collectors.toList());

        return panes;
    }

    private ContentPane create(Path path) {
        ContentPane pane = null;

        if (FileManager.SHELL_EXTENSIONS.contains(XFiles.getFileExtension(path))) {
            try {
                String input = Files.readString(path);
                pane = new ShellPane(path, input);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void save(ContentPane contentPane, Runnable onSucceded, Runnable onFailed) {
        var pane = (ShellPane) contentPane;
        String output = pane.getConsolePane().getInputArea().getText();
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
}
