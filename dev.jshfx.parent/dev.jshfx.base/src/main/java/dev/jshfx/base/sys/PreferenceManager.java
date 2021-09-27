package dev.jshfx.base.sys;

import java.nio.file.Path;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.j.util.prefs.FilePreferencesFactory;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public final class PreferenceManager extends Manager {

	private static final PreferenceManager INSTANCE = new PreferenceManager();

	private static final String DEFAULT_THEME_COLOR = "#ecececff";
	private StringProperty themeColorStyle = new SimpleStringProperty();
	private ObservableList<Color> customColors;

	private PreferenceManager() {
	}

	public static PreferenceManager get() {
		return INSTANCE;
	}

	@Override
	public void init() throws Exception {
		System.setProperty(FilePreferencesFactory.DEFAULT_SYSTEM_PREFERENCES_PROPERTY,
				FileManager.DEFAULT_PREFS_FILE.toString());
		System.setProperty(FilePreferencesFactory.SYSTEM_PREFERENCES_PROPERTY, FileManager.USER_PREFS_FILE.toString());
		System.setProperty(FilePreferencesFactory.DEFAULT_USER_PREFERENCES_PROPERTY,
				FileManager.DEFAULT_PREFS_FILE.toString());
		System.setProperty(FilePreferencesFactory.USER_PREFERENCES_PROPERTY, FileManager.USER_PREFS_FILE.toString());
		System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());

		FXResourceBundle.setLocale(getLocale());
		setThemeColorStyle(getThemeColorString());
	}

	private String getLocale() {
		return Preferences.userRoot().node("/region").get("locale", "en");
	}

	private String getThemeColorString() {
		return Preferences.userRoot().node("/theme").get("color", DEFAULT_THEME_COLOR);
	}

	public Color getThemeColor() {
		return Color.web(getThemeColorString());
	}

	public void setThemeColor(Color value) {
		var str = value.toString().replace("0x", "#");
		Preferences.userRoot().node("/theme").put("color", str);
		setThemeColorStyle(str);
	}

	public List<Color> getCustomThemeColors() {
		String colorString = Preferences.userRoot().node("/theme").get("colors", "[]");
		List<String> colorStrings = JsonUtils.get().fromJson(colorString, List.class);
		if (!colorStrings.contains(DEFAULT_THEME_COLOR)) {
			colorStrings.add(0, DEFAULT_THEME_COLOR);
		}

		var colors = colorStrings.stream().map(c -> Color.web(c)).collect(Collectors.toList());

		return colors;
	}

	public void setCustomThemeColors(List<Color> value) {
		var colorStrings = value.stream().map(color -> color.toString().replace("0x", "#"))
				.collect(Collectors.toList());
		String string = JsonUtils.get().toJson(colorStrings);
		Preferences.userRoot().node("/theme").put("colors", string);
	}

	private void setThemeColorStyle(String value) {
		themeColorStyle.set(String.format("-fx-base: %s;", value));
	}

	public StringProperty themeColorStyleProperty() {
		return themeColorStyle;
	}

	public ObservableList<Color> getCustomColors() {

		if (customColors == null) {
			String colorString = Preferences.userRoot().node("/scene").get("colors", "[]");
			List<String> colorStrings = JsonUtils.get().fromJson(colorString, List.class);
			customColors = FXCollections
					.observableArrayList(colorStrings.stream().map(c -> Color.web(c)).collect(Collectors.toList()));
		}

		return customColors;
	}

	public void setCustomColors(List<Color> value) {
		customColors.setAll(value);
		var colorStrings = value.stream().map(color -> color.toString().replace("0x", "#"))
				.collect(Collectors.toList());
		String string = JsonUtils.get().toJson(colorStrings);
		Preferences.userRoot().node("/scene").put("colors", string);
	}

	public Path getInitialDirectory() {
		return Path.of(System.getProperty("user.home"));
	}
}
