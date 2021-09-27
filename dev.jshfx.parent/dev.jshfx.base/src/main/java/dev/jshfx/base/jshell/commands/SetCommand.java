package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.SetBox;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import picocli.CommandLine.Command;

@Command(name = "/set")
public class SetCommand extends BaseCommand {

	public SetCommand(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	@Override
	public void run() {
		Platform.runLater(() -> {

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.initOwner(commandProcessor.getSession().getWindow());
			dialog.setTitle(FXResourceBundle.getBundle().getString​("settings"));

			SetBox setBox = new SetBox(commandProcessor.getSession().loadSettings());
			DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.setContent(setBox);
			ButtonType okButtonType = new ButtonType(FXResourceBundle.getBundle().getString​("ok"), ButtonData.OK_DONE);
			ButtonType cancelButtonType = new ButtonType(FXResourceBundle.getBundle().getString​("cancel"),
					ButtonData.CANCEL_CLOSE);
			dialogPane.getButtonTypes().addAll(okButtonType, cancelButtonType);

//            final Button btOk = (Button) dialogPane.lookupButton(okButtonType);
//            btOk.setOnAction(e -> {
//                dialog.close();
//                commandProcessor.getSession().setSettings(setBox.getSettings());
//            });
//            final Button cancel = (Button) dialogPane.lookupButton(cancelButtonType);
//            cancel.setOnAction(e -> dialog.close());

			dialog.showAndWait().filter(response -> response.getButtonData() == ButtonData.OK_DONE)
					.ifPresent(response -> commandProcessor.getSession().setSettings(setBox.getSettings()));
		});
	}
}
