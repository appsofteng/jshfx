package dev.jshfx.base.sys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import dev.jshfx.base.Constants;

public final class FileManager extends Manager {

	private static final FileManager INSTANCE = new FileManager();

	private static final String SYS_HOME_DIR = System.getProperty("user.home") + "/." + Constants.SYS_NAME + "/"
			+ Constants.SYS_VERSION;
	private static final Path USER_CONF_DIR = Path.of(SYS_HOME_DIR + "/conf");
	private static final Path LOG_DIR = Path.of(SYS_HOME_DIR + "/log");
	public static final Path USER_PREFS_FILE = Path.of(USER_CONF_DIR + "/preferences.properties");

	private static final String START_DIR = System.getProperty("user.dir");
	public static final Path DEFAULT_PREFS_FILE = Path.of(START_DIR + "/conf/preferences.properties");

	public static final Path HISTORY_FILE =  Path.of(USER_CONF_DIR + "/history.json");
    public static final Path ENV_FILE = Path.of(USER_CONF_DIR + "/env.json");
    public static final Path SETTINGS_FILE = Path.of(USER_CONF_DIR + "/settings.json");
    public static final Path ENVS_FILE = Path.of(USER_CONF_DIR + "/envs.json");
    
    public static final String LOGGING_CONF_FILE =  "logging.properties";
    
    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

	private FileManager() {
	}

	public static FileManager get() {
		return INSTANCE;
	}

	@Override
	public void init() throws IOException {
		Files.createDirectories(LOG_DIR);
		LogManager.getLogManager().readConfiguration(FileManager.class.getResourceAsStream(LOGGING_CONF_FILE));
		Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
		Files.createDirectories(USER_CONF_DIR);		
	}
	
	private void uncaughtException(Thread thread, Throwable throwable) {

		LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
	}
}
