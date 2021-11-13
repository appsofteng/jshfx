package dev.jshfx.base;

import org.controlsfx.glyphfont.GlyphFontRegistry;

import dev.jshfx.base.sys.CustomSecurityManager;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.base.sys.PreferenceManager;
import dev.jshfx.base.sys.RepositoryManager;
import dev.jshfx.base.sys.ResourceManager;
import dev.jshfx.base.sys.TaskManager;
import dev.jshfx.base.ui.RootPane;
import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    public static final double WINDOW_PREF_WIDTH;
    public static final double WINDOW_PREF_HEIGHT;
    
    static {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        WINDOW_PREF_WIDTH = screen.getWidth() * 0.7;
        WINDOW_PREF_HEIGHT = screen.getHeight() * 0.7;
    }
    
	private RootPane root;

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void init() throws Exception {
	    System.setSecurityManager(new CustomSecurityManager());
		FileManager.get().init();
		TaskManager.get().init();
		Fonts.getUrls().forEach((k, v) -> GlyphFontRegistry.register(k, v, 10));		
		FXResourceBundle.setDefaultCaller(MainApp.class);
		ResourceManager.get().setCaller(MainApp.class);		
		PreferenceManager.get().init();
		RepositoryManager.get().init();
	}

	@Override
	public void start(Stage stage) throws Exception {
		root = new RootPane();
		
		Scene scene = new Scene(root, WINDOW_PREF_WIDTH, WINDOW_PREF_HEIGHT, false,
				SceneAntialiasing.BALANCED);
		scene.getStylesheets().add(ResourceManager.get().getStyle());
		stage.setScene(scene);
		stage.setTitle(FXResourceBundle.getBundle().getString​("appName") + " " + Constants.SYS_VERSION);
		stage.getIcons().add(ResourceManager.get().getIconImage());

		stage.setOnCloseRequest(e -> {
		   root.getActions().getActionController().closeApp(e);
		});
		
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		root.dispose();
		TaskManager.get().stop();
		FileManager.get().stop();
	}
}
