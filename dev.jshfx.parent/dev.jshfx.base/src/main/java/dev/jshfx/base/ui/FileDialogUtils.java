package dev.jshfx.base.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.PreferenceManager;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class FileDialogUtils {

    private FileDialogUtils() {
    }

    public static Optional<Path> getDirectory(Window window) {
        Path initialDir = PreferenceManager.get().getLatestDir();
        Optional<Path> dir = null;
        var chooser = new DirectoryChooser();
        chooser.setInitialDirectory(initialDir.toFile());
        chooser.setTitle(FXResourceBundle.getBundle().getString​("directory"));
        var file = chooser.showDialog(window);

        if (file != null) {
            dir = Optional.of(file.toPath());
            PreferenceManager.get().setLatestDir(dir.get());
        }

        return dir;
    }

    public static List<Path> getJavaFiles(Window window) {
        List<Path> paths = List.of();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(FXResourceBundle.getBundle().getString​("Open"));
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Java", "*.jar", "*.java", "*.jmod", "*.jsh"),
                new ExtensionFilter("JAR", "*.jar"), new ExtensionFilter("Java", "*.java"),
                new ExtensionFilter("JMOD", "*.jmod"), new ExtensionFilter("JSH", "*.jsh"),
                new ExtensionFilter("XML", "*.xml"), new ExtensionFilter("*", "*.*"));
        fileChooser.setInitialDirectory(PreferenceManager.get().getLatestDir().toFile());
        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {

            paths = files.stream().map(File::toPath).collect(Collectors.toList());
            if (paths.size() > 0) {
                PreferenceManager.get().setLatestDir(paths.get(0).getParent());
            }
        }

        return paths;
    }

    public static Optional<Path> saveSourceJavaFile(Window window) {
        return saveSourceJavaFile(window, null);
    }

    public static Optional<Path> saveSourceJavaFile(Window window, Path initialPath) {
        return saveFile(window, initialPath, new ExtensionFilter("Java", "*.jsh", "*.java"));
    }
    
    public static Optional<Path> saveImageFile(Window window, Path initialPath) {
        return saveFile(window, initialPath, new ExtensionFilter(FXResourceBundle.getBundle().getString​("image"), "*.png"));
    }

    public static Optional<Path> saveFile(Window window, Path initialPath, ExtensionFilter... filter) {

        var initialDir = PreferenceManager.get().getLatestDir().toFile();
        String initialFile = null;
        
        if (initialPath != null) {

            if (initialPath.isAbsolute()) {
                initialDir = initialPath.getParent().toFile();
            }
            
            initialFile = initialPath.getFileName().toString();
        }

        Optional<Path> path = Optional.empty();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(FXResourceBundle.getBundle().getString​("save"));
        fileChooser.getExtensionFilters().addAll(filter);
        fileChooser.setInitialDirectory(initialDir);
        fileChooser.setInitialFileName(initialFile);
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            path = Optional.of(file.toPath());
            PreferenceManager.get().setLatestDir(path.get().getParent());
        }

        return path;
    }
}
