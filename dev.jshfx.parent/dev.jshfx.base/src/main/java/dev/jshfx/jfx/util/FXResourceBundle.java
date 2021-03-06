package dev.jshfx.jfx.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

public class FXResourceBundle {
	private static ObjectProperty<Locale> locale;
	private static Class<?> defaultCaller;

	private static final String BUNDLE_DIR_NAME = "bundles";
	private static final String BUNDLE_FILE_NAME = "strings";

	private final String bundleDir;
	private Class<?> caller;
	private String baseName;
	private Module module;
	private FXResourceBundle parent;

	private static Map<String, FXResourceBundle> cache = new ConcurrentHashMap<>();

	private static final Logger LOGGER = Logger.getLogger(FXResourceBundle.class.getName());

	private Map<StringProperty, List<Object>> stringProperties = new WeakHashMap<>();

	private FXResourceBundle(Class<?> caller, String baseName, FXResourceBundle parent) {
		this.caller = caller;
		this.baseName = baseName;
		this.module = caller.getModule();
		this.parent = parent;

		String path = caller.getPackageName().replace(".", "/");
		this.bundleDir = path + "/" + BUNDLE_DIR_NAME + "/";

		LocaleProperty().addListener((v, o, n) -> {
			if (n != null) {
				stringProperties.keySet().forEach(k -> {
					var s = stringProperties.get(k);
					k.set(getStringâ€‹(s.get(0).toString(), (Object[]) s.get(1)));
				});
			}
		});
	}

	public static Locale getLocale() {

		return LocaleProperty().get();
	}

	public static void setLocale(String value) {
		LocaleProperty().set(Locale.forLanguageTag(value));
	}

	public static ObjectProperty<Locale> LocaleProperty() {

		if (locale == null) {
			locale = new SimpleObjectProperty<>(Locale.getDefault());
		}

		return locale;
	}

	public static void setDefaultCaller(Class<?> defaultCaller) {
		FXResourceBundle.defaultCaller = defaultCaller;
	}

	public static FXResourceBundle getBundle() {
		Class<?> cls = FXResourceBundle.class;
		FXResourceBundle parent = null;

		if (defaultCaller != null) {
			cls = defaultCaller;
			parent = getBundleâ€‹(FXResourceBundle.class, null);
		}

		return getBundleâ€‹(cls, parent);
	}

	public static FXResourceBundle getBundleâ€‹(Class<?> caller) {
		return getBundleâ€‹(caller, getBundle());
	}

	public static FXResourceBundle getBundleâ€‹(Class<?> caller, FXResourceBundle parent) {
		String baseName = caller.getPackageName() + "." + BUNDLE_DIR_NAME + "." + BUNDLE_FILE_NAME;
		FXResourceBundle bundle = cache.computeIfAbsent(baseName, k -> new FXResourceBundle(caller, baseName, parent));

		return bundle;
	}

	public ResourceBundle getResourceBundle() {

		ResourceBundle bundle = null;
		
		try {
			if (module == null) {
				bundle = ResourceBundle.getBundle(baseName, getLocale());
			} else {
				bundle = ResourceBundle.getBundle(baseName, getLocale(), module);
			}
		} catch (MissingResourceException e) {
			if (parent != null) {
				bundle = parent.getResourceBundle();
			}
		}

		return bundle;
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(String key) {
		Object value = null;

		try {

			ResourceBundle bundle = getResourceBundle();
			if (bundle != null) {
				value = bundle.getObject(key);
			} else {
				throw new MissingResourceException("Cannot find object for the key", caller.getName(), key);
			}

		} catch (MissingResourceException e) {
			if (parent != null) {
				value = parent.getObject(key);
			} else {
				LOGGER.log(Level.INFO,
						String.format("%s, key: %s, class: %s", e.getMessage(), e.getKey(), e.getClassName()), e);
			}
		}

		return (T) value;
	}

	public String getStringâ€‹(String key, Object... args) {

		return getStringOrDefault(key, key, args);
	}
	
    public Map<String, String> getStrings(Set<String> keys) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> getStringâ€‹(k, (Object[])null)));
    }

	public void put(StringProperty property, String key, Object... args) {
		property.set(getStringâ€‹(key, args));
		stringProperties.put(property, List.of(key, args));
	}

	public StringBinding getStringBinding(String key, Object... args) {
		return Bindings.createStringBinding(() -> getStringâ€‹(key, args), LocaleProperty());
	}

	public StringBinding getStringBinding(ReadOnlyObjectProperty<?> key, Object... args) {
		return Bindings.createStringBinding(() -> getStringâ€‹(key.getValue().toString().toLowerCase(), args), key,
				LocaleProperty());
	}

	public String getStringOrDefault(String key, String defaultValue, Object... args) {
		String value = getObject(key);

		if (value == null) {
			value = defaultValue;
		}

		return MessageFormat.format(value, args);
	}

	public String getStringMaxWidth(String key, String arg, int maxWidth) {
		arg = arg.replaceAll("[\\n\\r]+", " ");
		String text = String.format("%." + maxWidth + "s%s", arg, arg.length() > maxWidth ? "..." : "");

		return getStringâ€‹(key, text);
	}

	public Set<String> getLocales() {
		Set<String> locales = Set.of();

		try {

			final File jarFile = new File(caller.getProtectionDomain().getCodeSource().getLocation().getPath());

			if (jarFile.isFile()) {
				final JarFile jar = new JarFile(jarFile);

				locales = jar.stream().map(JarEntry::getName).filter(n -> n.startsWith(bundleDir))
						.filter(n -> n.length() > bundleDir.length())
						.map(n -> n.substring(bundleDir.length(), n.lastIndexOf("."))).filter(n -> n.contains("_"))
						.map(n -> n.substring(n.indexOf("_") + 1).replace("_", "-")).collect(Collectors.toSet());

				jar.close();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return locales;
	}
}
