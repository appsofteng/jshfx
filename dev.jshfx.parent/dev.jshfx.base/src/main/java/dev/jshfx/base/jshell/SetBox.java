package dev.jshfx.base.jshell;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class SetBox extends VBox {

	private Settings settings;
	private CheckBox defaultCheck;
	private CheckBox printingCheck;
	private CheckBox scriptsCheck;
	private ListView<String> scriptsView;

	public SetBox(Settings settings) {
		this.settings = settings;
		setGraphics();
		setBehavior();
		setContextMenu();
	}

	private void setGraphics() {
		defaultCheck = new CheckBox(FXResourceBundle.getBundle().getString​("default"));
		defaultCheck.setTooltip(new Tooltip());
		defaultCheck.getTooltip().setText(FXResourceBundle.getBundle().getString​("/open.default"));
		defaultCheck.setSelected(settings.isLoadDefault());

		printingCheck = new CheckBox(FXResourceBundle.getBundle().getString​("printing"));
		printingCheck.setTooltip(new Tooltip());
		printingCheck.getTooltip().setText(FXResourceBundle.getBundle().getString​("/open.printing"));
		printingCheck.setSelected(settings.isLoadPrinting());

		scriptsCheck = new CheckBox(FXResourceBundle.getBundle().getString​("loadScripts"));
		scriptsCheck.setTooltip(new Tooltip());
		scriptsCheck.getTooltip().setText(FXResourceBundle.getBundle().getString​("/open.loadScripts"));
		scriptsCheck.setSelected(settings.isLoadScripts());
		scriptsCheck.setPadding(new Insets(5, 0, 0, 0));

		scriptsView = new ListView<>(FXCollections.observableList(settings.getStartupScripts()));
		scriptsView.setPrefSize(500, 300);
		scriptsView.setMinHeight(300);
		scriptsView.setMaxHeight(300);

		getChildren().addAll(defaultCheck, printingCheck, scriptsCheck, scriptsView);
	}

	private void setBehavior() {
		defaultCheck.setOnAction(e -> settings.setLoadDefault(defaultCheck.isSelected()));
		printingCheck.setOnAction(e -> settings.setLoadPrinting(printingCheck.isSelected()));
		scriptsCheck.setOnAction(e -> settings.setLoadScripts(scriptsCheck.isSelected()));
	}

	private void setContextMenu() {
		MenuItem addFiles = new MenuItem(FXResourceBundle.getBundle().getString​("addScripts"));
		addFiles.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(FXResourceBundle.getBundle().getString​("startupScripts"));
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JShell", "*.jsh"));

			List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getScene().getWindow());

			if (selectedFiles != null) {
				scriptsView.getItems().addAll(selectedFiles.stream().map(f -> f.toString())
						.filter(p -> !settings.getStartupScripts().contains(p)).collect(Collectors.toList()));
			}
		});

		MenuItem removeSelection = new MenuItem(FXResourceBundle.getBundle().getString​("removeSelection"));
		removeSelection.disableProperty().bind(scriptsView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
		removeSelection.setOnAction(e -> {
			scriptsView.getItems().removeAll(scriptsView.getSelectionModel().getSelectedItems());
		});

		ContextMenu menu = new ContextMenu(addFiles, removeSelection);
		scriptsView.setContextMenu(menu);
	}

	public Settings getSettings() {
		return settings;
	}
}
