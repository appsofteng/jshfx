package dev.jshfx.jx.tools;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.jshfx.base.MainApp;
import dev.jshfx.jfx.util.FXResourceBundle;

public class JavaSourceTest {

    private static JavaSource javaSource;
    private static final String EXAMPLE_CLASS = "dev.jshfx.jx.tools.Example";
    private Map<String, String> typeMapping = Map.of("dev.jshfx.jx.tools.Example", "dev.jshfx.jx.tools.Example");
            
    @BeforeAll
    public static void setup() {
        FXResourceBundle.setDefaultCaller(MainApp.class);
        javaSource = new JavaSource().setResourceBundle(FXResourceBundle.getBundle().getResourceBundle());
        javaSource.setSourcePaths(List.of(Path.of("src/test/java")));
    }
//    
//    @Test
//    public void testGetHtmlDocClass() {       
//        var signature = Signature.get(EXAMPLE_CLASS, (t,i) -> EXAMPLE_CLASS);
//        
//        var doc = javaSource.getHtmlDoc(signature);
//        
//        System.out.println(doc);
//    }
//    
    @Test
    public void testGetHtmlDocClassEnum() {       
        var signature = Signature.get("dev.jshfx.jx.tools.Example.SubExample", null, (type,imports) -> typeMapping.get(type));
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
    }
//    
//    @Test
//    public void testGetHtmlDocField() {
//        var signature = Signature.get("Example.FIELD:String", (t,i) -> EXAMPLE_CLASS);
//        
//        var doc = javaSource.getHtmlDoc(signature);
//        
//     //   System.out.println(doc);
//    }
//    
//    @Test
//    public void testGetHtmlDocMethod() {
//        var signature = Signature.get("void Example.method()", (t,i) -> EXAMPLE_CLASS);
//        
//        var doc = javaSource.getHtmlDoc(signature);
//        
//        System.out.println(doc);
//    }
//    
//    @Test
//    public void testGetHtmlDocJar() {
//        javaSource.setSourcePaths(List.of(Path.of("src/test/resources/dev.jar")));
//        var signature = Signature.get(EXAMPLE_CLASS,(t,i) -> EXAMPLE_CLASS);
//        
//        var doc = javaSource.getHtmlDoc(signature);
//        System.out.println(doc);
//    }
}
