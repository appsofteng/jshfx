package dev.jshfx.jfx.scene.control;

import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.scene.control.ButtonType;

public final class ButtonTypes {

    public static final ButtonType YES_ALL = new ButtonType(FXResourceBundle.getBundle().getString​("yesAll"));
    public static final ButtonType NO_ALL = new ButtonType(FXResourceBundle.getBundle().getString​("noAll"));
    
    private ButtonTypes() {
    }
}
