module dev.jshfx.base {
	
	requires jdk.jshell;
    requires transitive jdk.jsobject;
	
	requires java.compiler;
    requires java.logging;
    requires transitive java.prefs;
	
	requires dev.jshfx.fonts;
	
	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
    requires transitive javafx.web;
	
	requires org.controlsfx.controls;
	
	requires org.fxmisc.richtext;
	requires flowless;
	requires reactfx;
    requires wellbehavedfx;
    requires org.fxmisc.undo;
	
    requires jakarta.json.bind;
    requires org.eclipse.yasson;
    
    requires com.github.javaparser.core;
    requires info.picocli;
    requires org.apache.maven.resolver;
    requires org.apache.maven.resolver.impl;
    requires maven.resolver.provider;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.transport.file;
    requires org.apache.maven.resolver.transport.http;
	
    exports dev.jshfx.j.util.prefs;
    
	opens dev.jshfx.base to javafx.graphics;
    opens dev.jshfx.base.jshell to org.eclipse.yasson, javafx.base;
    opens dev.jshfx.base.jshell.commands to info.picocli;
}