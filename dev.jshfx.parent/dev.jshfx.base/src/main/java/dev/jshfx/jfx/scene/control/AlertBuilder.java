package dev.jshfx.jfx.scene.control;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

public final class AlertBuilder {

    private Alert alert;

    private AlertBuilder(AlertType type) {
        alert = new Alert(type);
        alert.setHeaderText(null);
        // alert.getDialogPane().styleProperty().bind(PreferenceManager.get().themeColorStyleProperty());
    }

    public static AlertBuilder get(AlertType type) {
        return new AlertBuilder(type);
    }

    public AlertBuilder initOwner(Window window) {
        alert.initOwner(window);

        return this;
    }

    public AlertBuilder contentTextKey(String key, String... args) {

        alert.setContentText(FXResourceBundle.getBundle().getStringâ€‹(key, args));
        return this;
    }

    public AlertBuilder expandableContent(Throwable content) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        content.printStackTrace(pw);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);

        alert.getDialogPane().setExpandableContent(textArea);

        return this;
    }

    public AlertBuilder buttonTypes(ButtonType... buttons) {
        alert.getDialogPane().getButtonTypes().setAll(buttons);
        return this;
    }

    public AlertBuilder buttonTypes(List<ButtonType> buttons) {
        alert.getDialogPane().getButtonTypes().setAll(buttons);
        return this;
    }

    public Alert build() {
        return alert;
    }
}
