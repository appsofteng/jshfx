package dev.jshfx.base.jshell;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.jshfx.base.sys.FileManager;

public class Settings {

    public static final String DEFAULT = "DEFAULT";
    public static final String PRINTING = "PRINTING";
    public static final Map<String,String> PREDEFINED_STARTUP_FILES = Map.of(DEFAULT, "start-default.txt", PRINTING, "start-printing.txt");
    private static final String DEFAULT_ENV_NAME = "env";
    private String feedbackMode = Feedback.NORMAL;
    private boolean loadStartupFiles;
    private Set<String> predefinedStartupFiles = new HashSet<>();
    private Set<String> startupFiles = new HashSet<>();
    private String env = DEFAULT_ENV_NAME;

    public String getFeedbackMode() {
        return feedbackMode;
    }

    public void setFeedbackMode(String feedbackMode) {
        this.feedbackMode = feedbackMode;
    }

    public Set<String> getPredefinedStartupFiles() {
        return predefinedStartupFiles;
    }

    public void setPredefinedStartupFiles(Set<String> predefinedStartupFiles) {
        this.predefinedStartupFiles = predefinedStartupFiles;
    }

    public boolean isLoadStartupFiles() {
        return loadStartupFiles;
    }

    public void setLoadStartupFiles(boolean value) {
        this.loadStartupFiles = value;
    }

    public Set<String> getStartupFiles() {
        return startupFiles;
    }

    public void setStartupFiles(Set<String> startupScripts) {
        this.startupFiles = startupScripts;
    }
    
    public String getEnv() {
        return env;
    }
    
    public void setEnv(String env) {
        this.env = env;
    }
}
