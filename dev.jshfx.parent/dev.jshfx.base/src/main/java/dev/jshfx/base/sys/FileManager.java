package dev.jshfx.base.sys;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import dev.jshfx.base.Constants;
import dev.jshfx.j.util.LU;

public final class FileManager extends Manager {

    private static final FileManager INSTANCE = new FileManager();

    private static final String SYS_HOME_DIR = System.getProperty("user.home") + "/." + Constants.SYS_NAME + "/"
            + Constants.SYS_VERSION;
    private static final Path USER_CONF_DIR = Path.of(SYS_HOME_DIR + "/conf");
    private static final Path LOG_DIR = Path.of(SYS_HOME_DIR + "/log");

    public static final Path USER_ENV_DIR = Path.of(USER_CONF_DIR + "/env");

    public static final Path USER_PREFS_FILE = Path.of(USER_CONF_DIR + "/preferences.properties");

    private static final String START_DIR = System.getProperty("user.dir");
    public static final Path DEFAULT_PREFS_FILE = Path.of(START_DIR, "conf/preferences.properties");
    private static final Path JDK_SOURCE_FILE = Path.of(START_DIR, "lib/src.zip");
    public static final String UTIL_CLASSPATH = START_DIR +  "/lib/dev.jshfx.util.jar";

    public static final Path HISTORY_FILE = Path.of(USER_CONF_DIR + "/history.json");
    public static final Path SET_FILE = Path.of(USER_CONF_DIR + "/set.json");

    public static final String LOGGING_CONF_FILE = "logging.properties";

    private static final String CONFIG_FILE_EXTENSION = ".json";
    public static final String JAVA = "java";
    public static final String JSH = "jsh";
    public static final List<String> EXTENSIONS = List.of(JAVA, JSH);
    public static final List<String> SHELL_EXTENSIONS = List.of(JAVA, JSH);

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());
    private PrintStream err;
    private PrintStream out;
    
    private FileSystem jdkSource;
    private List<Path> jdkPaths;

    private FileManager() {
    }

    public static FileManager get() {
        return INSTANCE;
    }

    @Override
    public void init() throws IOException {
        err = System.err;
        out = System.out;
        Files.createDirectories(LOG_DIR);
        LogManager.getLogManager().readConfiguration(FileManager.class.getResourceAsStream(LOGGING_CONF_FILE));
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
        Files.createDirectories(USER_ENV_DIR);

        URI uri = URI.create("jar:" + JDK_SOURCE_FILE.toUri());
        jdkSource = FileSystems.newFileSystem(uri, Collections.emptyMap());
        jdkPaths = new ArrayList<>();
        Path root = jdkSource.getRootDirectories().iterator().next();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
            for (Path p : ds) {
                if (Files.isDirectory(p)) {
                    jdkPaths.add(p);
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        jdkSource.close();
    }

    public void restoreOutput() {
        System.setErr(err);
        System.setOut(out);
    }
    
    public List<Path> getJdkPaths() {
        return jdkPaths;
    }
    
    public Path getEnvFile(String name) {
        return Path.of(USER_ENV_DIR + "/" + name + FileManager.CONFIG_FILE_EXTENSION);
    }

    public Set<String> getEnvNames() {
        Set<String> names = new HashSet<>();
        try {
            names = Files.list(USER_ENV_DIR)
                    .map(p -> p.getFileName().toString().replaceFirst(CONFIG_FILE_EXTENSION + "$", ""))
                    .collect(Collectors.toCollection(() -> new HashSet<>()));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return names;
    }

    public void deleteEnvs(Set<String> names) {
        names.stream().map(n -> Path.of(USER_ENV_DIR + "/" + n + CONFIG_FILE_EXTENSION))
                .forEach(p -> LU.of(() -> Files.delete(p)));
    }

    private void uncaughtException(Thread thread, Throwable throwable) {

        LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
    }
}
