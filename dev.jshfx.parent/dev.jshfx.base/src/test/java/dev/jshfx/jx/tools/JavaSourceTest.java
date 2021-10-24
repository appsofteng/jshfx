package dev.jshfx.jx.tools;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.jshfx.base.MainApp;
import dev.jshfx.jfx.util.FXResourceBundle;

public class JavaSourceTest {

    private static JavaSource javaSource;
    
    @BeforeAll
    public static void setup() {
        FXResourceBundle.setDefaultCaller(MainApp.class);
        javaSource = new JavaSource().setResourceBundle(FXResourceBundle.getBundle().getResourceBundle());
    }
    
    @Test
    public void testGetHtmlDoc() {
        javaSource.setSourcePaths(List.of(Path.of("src/test/java")));
        var signature = Signature.get("dev.jshfx.jx.tools.Example", List.of());
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
    }
    
    @Test
    public void testGetHtmlDocJar() {
        javaSource.setSourcePaths(List.of(Path.of("src/test/resources/dev.jar")));
        var signature = Signature.get("dev.jshfx.jx.tools.Example", List.of());
        
        var doc = javaSource.getHtmlDoc(signature);
        System.out.println(doc);
    }
}
