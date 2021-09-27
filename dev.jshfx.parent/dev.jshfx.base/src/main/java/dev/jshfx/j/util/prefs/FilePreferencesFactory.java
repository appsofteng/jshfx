package dev.jshfx.j.util.prefs;

import java.nio.file.Path;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class FilePreferencesFactory implements PreferencesFactory {

	public static final String DEFAULT_SYSTEM_PREFERENCES_PROPERTY = "java.util.prefs.defaultSystemRoot";
	public static final String SYSTEM_PREFERENCES_PROPERTY = "java.util.prefs.systemRoot";
	public static final String DEFAULT_USER_PREFERENCES_PROPERTY = "java.util.prefs.defaultUserRoot";
	public static final String USER_PREFERENCES_PROPERTY = "java.util.prefs.userRoot";
	
    private static final String DEFAULT_SYSTEM_PREFERENCES = System.getProperty(DEFAULT_SYSTEM_PREFERENCES_PROPERTY);
    private static final String SYSTEM_PREFERENCES = System.getProperty(SYSTEM_PREFERENCES_PROPERTY);
    private static final String DEFAULT_USER_PREFERENCES = System.getProperty(DEFAULT_USER_PREFERENCES_PROPERTY);
    private static final String USER_PREFERENCES = System.getProperty(USER_PREFERENCES_PROPERTY);
    
    private Preferences systemRoot;
    private Preferences userRoot;

    @Override
    public Preferences systemRoot() {

        if (systemRoot == null) {
            systemRoot = new FilePreferences(null, "", Path.of(DEFAULT_SYSTEM_PREFERENCES), Path.of(SYSTEM_PREFERENCES));
        }

        return systemRoot;
    }

    @Override
    public Preferences userRoot() {
        if (userRoot == null) {
            userRoot = new FilePreferences(null, "", Path.of(DEFAULT_USER_PREFERENCES), Path.of(USER_PREFERENCES));
        }

        return userRoot;
    }
}
