package dev.jshfx.jx.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class SignatureTest {

    private Map<String, String> typeMapping = Map.of("ElementKind", "javax.lang.model.element.ElementKind", "String",
            "java.lang.String", "Double", "java.lang.Double");

    @Test
    public void testType() {
        String expectedTypeFullName = "java.lang.String";
        var signature = Signature.get(expectedTypeFullName, null, (type, imports) -> null);

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
        var signature = Signature.get("Double.BYTES:int", "int", (type, imports) -> typeMapping.get(type));

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
                (type, imports) -> typeMapping.get(type));

        assertEquals(Signature.Kind.ENUM_CONSTANT, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeFullName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedFullName, signature.getFullName());
    }

//
//    @Test
//    public void testMethodName0() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "chars";
//        List<String> expectedMethodParamTypes = List.of();
//
//        var signature = Signature.get("java.util.stream.IntStream String.chars()", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
//    
//    @Test
//    public void testMethodName1() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "charAt";
//        List<String> expectedMethodParamTypes = List.of("int");
//
//        var signature = Signature.get("char String.charAt(int index)", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
//    
//    @Test
//    public void testMethodName2() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "codePointCount";
//        List<String> expectedMethodParamTypes = List.of("int", "int");
//
//        var signature = Signature.get("int String.codePointCount(int beginIndex, int endIndex)", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
//    
//    @Test
//    public void testMethodNameVarArgs() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "formatted";
//        List<String> expectedMethodParamTypes = List.of("java.lang.Object...");
//
//        var signature = Signature.get("String String.formatted(Object...)", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : "java.lang.Object");
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
//    
//    @Test
//    public void testMethodNameArray() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "getBytes";
//        List<String> expectedMethodParamTypes = List.of("int", "int", "byte[]", "int");
//
//        var signature = Signature.get("void String.getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin)", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
//    
//    @Test
//    public void testMethodNameGeneric() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "transform";
//        List<String> expectedMethodParamTypes = List.of("java.util.function.Function");
//
//        var signature = Signature.get("R String.<R>transform(java.util.function.Function<? super String,? extends R> f)", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
//    
//    @Test
//    public void testMethodNameThrows() {
//        String expectedTypeFullName = "java.lang.String";
//        String expectedTypeSimpleName = "String";
//        String expectedMethodName = "getBytes";
//        List<String> expectedMethodParamTypes = List.of("java.lang.String");
//
//        var signature = Signature.get("byte[] String.getBytes(String charsetName) throws java.io.UnsupportedEncodingException", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);
//
//        assertEquals(Signature.Kind.METHOD, signature.getKind());
//        assertEquals(expectedTypeFullName, signature.getTypeFullName());
//        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
//        assertEquals(expectedMethodName, signature.getMethodName());
//        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
//    }
}
