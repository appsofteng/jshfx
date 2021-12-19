package dev.jshfx.base.jshell;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Settings {

    public static final String DEFAULT = "DEFAULT";
    public static final String PRINTING = "PRINTING";
    public static final Map<String,String> PREDEFINED_STARTUP_FILES = Map.of(DEFAULT, "start-default.txt", PRINTING, "start-printing.txt");
    private String feedbackMode = Feedback.NORMAL;
    private boolean loadStartupFiles = true;
    private Set<String> predefinedStartupFiles = new LinkedHashSet<>(List.of(DEFAULT, PRINTING));
    private Set<String> startupFiles = new LinkedHashSet<>();
    private Set<String> jshPaths = new LinkedHashSet<>();

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
    
    public Set<String> getJshPaths() {
        return jshPaths;
    }
    
    public void setJshPaths(Set<String> jshPaths) {
        this.jshPaths = jshPaths;
    }
}
