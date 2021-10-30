package dev.jshfx.base.sys;

import javafx.scene.image.Image;

public final class ResourceManager extends Manager {
	
    private static final ResourceManager INSTANCE = new ResourceManager();
    private static final String IMAGE_DIR = "images/";
    private static final String STYLE_DIR = "styles/";
    private static final String DEFAULT_STYLE = "style.css";
    private static final String ICON_IMAGE = "icon.png";
    private static final String DEFAULT_IMAGE = "default.png";
    private static Image iconImage;
    private static Image defaultImage;
   
	private Class<?> caller;
	
	private ResourceManager() {
	}
	
	public static ResourceManager get() {
		return INSTANCE;
	}
	
	public void setCaller(Class<?> caller) {
		this.caller = caller;
	}
	
	public Image getImage(String name) {
		return new Image(caller.getResourceAsStream(IMAGE_DIR + name));
	}	
	
	   public Image getImage(String name, double width, double height, boolean preserveRatio, boolean smooth) {
	        return new Image(caller.getResourceAsStream(IMAGE_DIR + name), width, height, preserveRatio, smooth);
	    }   
	
	public Image getIconImage() {
		if (iconImage == null) {
			iconImage = new Image(caller.getResourceAsStream(IMAGE_DIR + ICON_IMAGE));
		}
		
		return iconImage;
	}
	
	public Image getDefaultImage() {
		if (defaultImage == null) {
			defaultImage = new Image(caller.getResourceAsStream(IMAGE_DIR + DEFAULT_IMAGE));
		}
		
		return defaultImage;
	}
	
	public String getStyle() {
		return getStyle(DEFAULT_STYLE);
	}
	
	public String getStyle(String name) {
		return caller.getResource(STYLE_DIR + name).toExternalForm();
	}		
}
