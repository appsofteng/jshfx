package dev.jshfx.base.jshell;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private String feedbackMode = Feedback.NORMAL;
    private boolean loadDefault;
    private boolean loadPrinting;
    private boolean loadScripts;
    private List<String> startupScripts = new ArrayList<>();

    public String getFeedbackMode() {
        return feedbackMode;
    }
    
    public void setFeedbackMode(String feedbackMode) {
        this.feedbackMode = feedbackMode;
    }
    
    public boolean isLoadDefault() {
        return loadDefault;
    }

    public void setLoadDefault(boolean loadDefault) {
        this.loadDefault = loadDefault;
    }

    public boolean isLoadPrinting() {
        return loadPrinting;
    }

    public void setLoadPrinting(boolean loadPrinting) {
        this.loadPrinting = loadPrinting;
    }

    public boolean isLoadScripts() {
        return loadScripts;
    }

    public void setLoadScripts(boolean loadScripts) {
        this.loadScripts = loadScripts;
    }

    public List<String> getStartupScripts() {
        return startupScripts;
    }

    public void setStartupScripts(List<String> startupScripts) {
        this.startupScripts = startupScripts;
    }
}
