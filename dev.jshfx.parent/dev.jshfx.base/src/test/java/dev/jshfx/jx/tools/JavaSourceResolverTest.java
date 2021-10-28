package dev.jshfx.jx.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.jshfx.base.MainApp;
import dev.jshfx.jfx.util.FXResourceBundle;

public class JavaSourceResolverTest {

    private static JavaSourceResolver javaSource;
    private Map<String, String> typeMapping = Map.of("dev.jshfx.jx.tools.Example", "dev.jshfx.jx.tools.Example", "Example", "dev.jshfx.jx.tools.Example", "NestedEnum", "dev.jshfx.jx.tools.Example.NestedEnum",
            "String", "java.lang.String", "URI", "java.net.URI");
    private Function<String, String> resolveType = type -> typeMapping.get(type);
            
    @BeforeAll
    public static void setup() {
        FXResourceBundle.setDefaultCaller(MainApp.class);
        javaSource = new JavaSourceResolver().setResourceBundle(FXResourceBundle.getBundle().getResourceBundle());
    }
    
    @BeforeEach
    public void setupEach() {
        javaSource.setSourcePaths(List.of(Path.of("src/test/java")));
    }
    
    @Test
    public void testGetHtmlDocJar() {
        javaSource.setSourcePaths(List.of(Path.of("src/test/resources/dev.jar")));
        var signature = Signature.get("dev.jshfx.jx.tools.Example", null, resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Example documentation."));
    }
    
    @Test
    public void testGetHtmlDocClass() {       
        var signature = Signature.get("dev.jshfx.jx.tools.Example", null, resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("First sentence about example."));
    }
    
    @Test
    public void testGetHtmlDocField() {
        var signature = Signature.get("Example.FIELD:String", "String", resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Field documentation."));
    }
    
    @Test
    public void testGetHtmlDocMethod() {
        var signature = Signature.get("void Example.method()", null, resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Method documentation."));
    }
    
    @Test
    public void testGetHtmlDocMethodInt() {
        var signature = Signature.get("void Example.method(int i)", null, resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Method int documentation."));
    }
    
    @Test
    public void testGetHtmlDocMethodString() {
        var signature = Signature.get("void Example.method(String s)", null, resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Method String documentation."));
    }
    
    @Test
    public void testGetHtmlDocMethodURI() {
        var signature = Signature.get("void Example.method(URI u)", null, resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Method URI documentation."));
    }
    
    @Test
    public void testGetHtmlDocEnumConst() {
        var signature = Signature.get("NestedEnum.CONST", "NestedEnum", resolveType);
        
        var doc = javaSource.getHtmlDoc(signature);
        
        System.out.println(doc);
        
        assertTrue(doc.startsWith("Enum const documentation."));
    }
}
