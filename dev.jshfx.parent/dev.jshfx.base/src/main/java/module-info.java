module dev.jshfx.base {
	
	requires jdk.jshell;
	requires jdk.compiler;
    requires transitive jdk.jsobject;
	
	requires java.compiler;
	requires java.desktop;
    requires java.logging;
    requires transitive java.prefs;
	
	requires dev.jshfx.fonts;
	
	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.swing;
    requires transitive javafx.web;
	
	requires org.controlsfx.controls;
	
	requires org.fxmisc.richtext;
	requires org.fxmisc.flowless;
	requires reactfx;
    requires wellbehavedfx;
    requires org.fxmisc.undo;
	
    requires jakarta.json.bind;
    requires org.eclipse.yasson;
    
    requires info.picocli;
    requires dev.jshfx.automatic;
    requires dev.jshfx.util;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
	
    exports dev.jshfx.j.util.prefs;
    
	opens dev.jshfx.base to javafx.graphics;
    opens dev.jshfx.base.jshell to org.eclipse.yasson, javafx.base;
    opens dev.jshfx.base.jshell.commands to info.picocli;
    
    provides dev.jshfx.util.sys.JShService with dev.jshfx.base.sys.JShServiceImpl;
}