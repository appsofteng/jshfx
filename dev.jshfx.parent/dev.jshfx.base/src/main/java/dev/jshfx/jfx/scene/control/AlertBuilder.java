package dev.jshfx.jfx.scene.control;

import java.io.PrintWriter;
import java.io.StringWriter;

import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

public final class AlertBuilder {

	private Alert alert;
	
	private AlertBuilder(AlertType type) {
		alert = new Alert(type);
	//	alert.getDialogPane().styleProperty().bind(PreferenceManager.get().themeColorStyleProperty());
	}
	
	public static AlertBuilder get(AlertType type) {
		return new AlertBuilder(type);
	}
	
	public AlertBuilder contentTextKey(String key, String ... args) {
		
		alert.setContentText(FXResourceBundle.getBundle().getString​(key, args));
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
	
	public Alert build() {
		return alert;
	}
}
