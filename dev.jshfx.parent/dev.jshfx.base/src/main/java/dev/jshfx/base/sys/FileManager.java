package dev.jshfx.base.sys;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import dev.jshfx.j.module.ModuleUtils;
import dev.jshfx.j.util.LU;

public final class FileManager extends Manager {

    private static final FileManager INSTANCE = new FileManager();

    public static final String JAVA_RELEASE = getJavaRelease();

    static final Path SYS_HOME_DIR = Path.of(System.getProperty("user.home"), "/." + Constants.SYS_NAME);
    static final Path SYS_VERSION_DIR = SYS_HOME_DIR.resolve(Constants.SYS_VERSION);
    static final String CONF = "conf";
    static final Path USER_CONF_DIR = SYS_VERSION_DIR.resolve(CONF);
    private static final Path LOG_DIR = SYS_VERSION_DIR.resolve("log");

    public static final Path USER_ENV_DIR = Path.of(USER_CONF_DIR + "/env");

    public static final Path USER_PREFS_FILE = Path.of(USER_CONF_DIR + "/preferences.properties");

    private static final String START_DIR = System.getProperty("user.dir");
    public static final Path DEFAULT_PREFS_FILE = Path.of(START_DIR, "conf/preferences.properties");
    private static final Path JDK_SOURCE_FILE = Path.of(START_DIR, "src/java-src.zip");
    public static final Path EXT_DIR =  Path.of(START_DIR, "modules", "ext");
    private static final Path FX_DIR = Path.of(START_DIR, "lib", "fx");
    private static final String FX_MODULES = ModuleUtils.getModuleNames(FX_DIR);
    private static final String FX_CLASSPATH = getClassPath(FX_DIR);
    private static final String ACCESS_CLASSPATH = getClassPath(EXT_DIR);
    private static final Path SOURCE_DIR = Path.of(START_DIR, "src/lib");

    public static final Path HISTORY_FILE = Path.of(USER_CONF_DIR + "/history.json");
    public static final Path SET_FILE = Path.of(USER_CONF_DIR + "/set.json");

    public static final String LOGGING_CONF_FILE = "logging.properties";

    private static final String CONFIG_FILE_EXTENSION = ".json";
    public static final String JAVA = "java";
    public static final String JSH = "jsh";
    public static final List<String> EXTENSIONS = List.of(JAVA, JSH);
    public static final List<String> SHELL_EXTENSIONS = List.of(JAVA, JSH);

    public static final Path FIND_SUGGESTONS_FILE = Path.of(USER_CONF_DIR + "/find-suggestions.json");
    public static final Path REPLACE_SUGGESTONS_FILE = Path.of(USER_CONF_DIR + "/replace-suggestions.json");

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());
    private InputStream in;
    private PrintStream err;
    private PrintStream out;

    private FileSystem jdkSource;
    private List<Path> sourcePaths;

    private FileManager() {
    }

    public static FileManager get() {
        return INSTANCE;
    }

    @Override
    public void init() throws IOException {
        
        if (Files.notExists(SYS_VERSION_DIR)) {
            new Upgrader().upgrade();
        }
        
        in = System.in;
        err = System.err;
        out = System.out;
        Files.createDirectories(LOG_DIR);
        LogManager.getLogManager().readConfiguration(FileManager.class.getResourceAsStream(LOGGING_CONF_FILE));
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
        Files.createDirectories(USER_ENV_DIR);

        loadSourceCodes();
    }

    @Override
    public void stop() throws Exception {
        jdkSource.close();
    }

    public void restoreIO() {
        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }

    public List<Path> getSourcePaths() {
        return sourcePaths;
    }

    public String getClassPath() {
        return ACCESS_CLASSPATH + File.pathSeparator + FX_CLASSPATH;
    }

    public String getModulePath() {
        return FX_DIR.toString();
    }

    public String getModules() {
        return FX_MODULES;
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

    private void loadSourceCodes() throws IOException {
        URI uri = URI.create("jar:" + JDK_SOURCE_FILE.toUri());
        jdkSource = FileSystems.newFileSystem(uri, Collections.emptyMap());
        sourcePaths = new ArrayList<>();
        Path root = jdkSource.getRootDirectories().iterator().next();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
            for (Path p : ds) {
                if (Files.isDirectory(p)) {
                    sourcePaths.add(p);
                }
            }
        }

        Files.list(SOURCE_DIR).forEach(p -> sourcePaths.add(p));
    }

    private static String getClassPath(Path dir) {
        String path = "";
        try {
            path = Files.list(dir).map(Path::toString).collect(Collectors.joining(File.pathSeparator));
        } catch (IOException e) {

            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return path;
    }

    private static String getJavaRelease() {
        var release = System.getProperty("java.version");
        int i = release.indexOf('.');

        if (i > -1) {
            release = release.substring(0, i);
        }

        return release;
    }

    private void uncaughtException(Thread thread, Throwable throwable) {

        LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
    }
}
