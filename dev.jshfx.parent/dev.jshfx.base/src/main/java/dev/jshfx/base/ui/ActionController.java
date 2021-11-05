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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;

public class ActionController {

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

        if (tabs.size() > 1) {
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

        var shellPane = create(Path.of(XFiles.appendFileExtension(name, FileManager.JAVA)));        
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
                String input = "";
                if (path.isAbsolute()) {
                    input = Files.readString(path);
                }
                pane = new ShellPane(path, input);
                pane.setActions(rootPane.getActions());
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
