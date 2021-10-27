package dev.jshfx.jx.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class SignatureTest {

    private Map<String, String> typeMapping = Map.of("ElementKind", "javax.lang.model.element.ElementKind", "Double", "java.lang.Double", "Object", "java.lang.Object", "String",
            "java.lang.String");
    private Function<String, String> resolveType = type -> typeMapping.get(type);

    @Test
    public void testType() {
        String expectedTypeFullName = "java.lang.String";
        var signature = Signature.get(expectedTypeFullName, null, resolveType);

        assertEquals(Signature.Kind.TYPE, signature.getKind());
        assertEquals(expectedTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedTypeFullName, signature.getFullName());
    }

    @Test
    public void testFieldName() {
        String expectedTopTypeFullName = "java.lang.Double";
        String expectedTypeFullName = "java.lang.Double";
        String expectedFullName = "java.lang.Double.BYTES";
        var signature = Signature.get("Double.BYTES:int", "int", resolveType);

        assertEquals(Signature.Kind.FIELD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
    }

    @Test
    public void testEnumConstant() {
        String expectedTopTypeFullName = "javax.lang.model.element.ElementKind";
        String expectedTypeFullName = "javax.lang.model.element.ElementKind";
        String expectedFullName = "javax.lang.model.element.ElementKind.ANNOTATION_TYPE";

        var signature = Signature.get("ElementKind.ANNOTATION_TYPE", "ElementKind",
                resolveType);

        assertEquals(Signature.Kind.ENUM_CONSTANT, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
    }


    @Test
    public void testMethod0() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.chars";        
        List<String> expectedMethodParamTypes = List.of();
        
        var signature = Signature.get("java.util.stream.IntStream String.chars()", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodName1() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.charAt";
        List<String> expectedMethodParamTypes = List.of("int");

        var signature = Signature.get("char String.charAt(int index)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodName2() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.codePointCount";
        List<String> expectedMethodParamTypes = List.of("int", "int");

        var signature = Signature.get("int String.codePointCount(int beginIndex, int endIndex)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodNameVarArgs() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.formatted";
        List<String> expectedMethodParamTypes = List.of("java.lang.Object...");

        var signature = Signature.get("String String.formatted(Object...)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodNameArray() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.getBytes";
        List<String> expectedMethodParamTypes = List.of("int", "int", "byte[]", "int");

        var signature = Signature.get("void String.getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodNameGeneric() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.transform";
        List<String> expectedMethodParamTypes = List.of("java.util.function.Function");

        var signature = Signature.get("R String.<R>transform(java.util.function.Function<? super String,? extends R> f)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
}
