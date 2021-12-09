package dev.jshfx.jx.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class SignatureTest {

    private Map<String, String> typeMapping = Map.of("java.lang.Thread","java.lang.Thread", "java.util.AbstractMap", "java.util.AbstractMap", "ElementKind", "javax.lang.model.element.ElementKind", "Double", "java.lang.Double", "Object", "java.lang.Object", "String",
            "java.lang.String", "Example", "org.example.Example", "List", "java.util.List", "Map", "java.util.Map");
    private Function<String, String> resolveType = type -> typeMapping.get(type);

    @Test
    public void testType() {
        String expectedTypeFullName = "java.lang.String";
        var signature = Signature.get(expectedTypeFullName, null, resolveType);

        assertEquals(Signature.Kind.TYPE, signature.getKind());
        assertEquals(expectedTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedTypeFullName, signature.getCanonicalName());
    }

    @Test
    public void testFieldName() {
        String expectedTopTypeFullName = "java.lang.Double";
        String expectedTypeFullName = "java.lang.Double";
        String expectedFullName = "java.lang.Double.BYTES";
        var signature = Signature.get("Double.BYTES:int", "int", resolveType);

        assertEquals(Signature.Kind.FIELD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
    }

    @Test
    public void testEnumConstant() {
        String expectedTopTypeFullName = "javax.lang.model.element.ElementKind";
        String expectedTypeFullName = "javax.lang.model.element.ElementKind";
        String expectedFullName = "javax.lang.model.element.ElementKind.ANNOTATION_TYPE";

        var signature = Signature.get("ElementKind.ANNOTATION_TYPE", "ElementKind",
                resolveType);

        assertEquals(Signature.Kind.ENUM_CONSTANT, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
    }


    @Test
    public void testMethod0() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.chars";        
        List<String> expectedMethodParamTypes = List.of();
        
        var signature = Signature.get("java.util.stream.IntStream String.chars()", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethod1() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.charAt";
        List<String> expectedMethodParamTypes = List.of("int");

        var signature = Signature.get("char String.charAt(int index)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethod2() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.codePointCount";
        List<String> expectedMethodParamTypes = List.of("int", "int");

        var signature = Signature.get("int String.codePointCount(int beginIndex, int endIndex)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodVarArgs() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.formatted";
        List<String> expectedMethodParamTypes = List.of("java.lang.Object[]");
 
        var signature = Signature.get("String String.formatted(Object...)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodArray() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.getBytes";
        List<String> expectedMethodParamTypes = List.of("int", "int", "byte[]", "int");

        var signature = Signature.get("void String.getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodGeneric() {
        String expectedTopTypeFullName = "java.lang.String";
        String expectedTypeFullName = "java.lang.String";
        String expectedFullName = "java.lang.String.transform";
        List<String> expectedMethodParamTypes = List.of("java.util.function.Function");

        var signature = Signature.get("R String.<R>transform(java.util.function.Function<? super String,? extends R> f)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodGenericMore() {
        String expectedTopTypeFullName = "org.example.Example";
        String expectedTypeFullName = "org.example.Example";
        String expectedFullName = "org.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("java.util.Map");

        var signature = Signature.get("void Example.method(Map<String, List<Double>> map)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodGeneric2() {
        String expectedTopTypeFullName = "org.example.Example";
        String expectedTypeFullName = "org.example.Example";
        String expectedFullName = "org.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("java.util.List", "java.util.List", "java.util.Map");

        var signature = Signature.get("void Example.method(List<String> list1, List<List<Double>> list2, Map<String, List<Double>> map)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodGenericSpaces() {
        String expectedTopTypeFullName = "com.example.Example";
        String expectedTypeFullName = "com.example.Example";
        String expectedFullName = "com.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("java.util.List", "java.util.List", "java.util.Map");

        var signature = Signature.get("org.pckg.Type<A, B> com.example.Example<X, Y>.method(List<String> list1, List<List<Double>> list2, Map<String, List<Double>> map)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodType() {
        String expectedTopTypeFullName = "org.example.Example";
        String expectedTypeFullName = "org.example.Example";
        String expectedFullName = "org.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("int");

        var signature = Signature.get("void Example.method(int)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodArrayType() {
        String expectedTopTypeFullName = "org.example.Example";
        String expectedTypeFullName = "org.example.Example";
        String expectedFullName = "org.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("int[]");

        var signature = Signature.get("void Example.method(int[])", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethod2DArrayType() {
        String expectedTopTypeFullName = "org.example.Example";
        String expectedTypeFullName = "org.example.Example";
        String expectedFullName = "org.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("int[][]");

        var signature = Signature.get("void Example.method(int[][])", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testMethodTypes() {
        String expectedTopTypeFullName = "org.example.Example";
        String expectedTypeFullName = "org.example.Example";
        String expectedFullName = "org.example.Example.method";
        List<String> expectedMethodParamTypes = List.of("int", "java.lang.String");

        var signature = Signature.get("void Example.method(int, String)", null, resolveType);

        assertEquals(Signature.Kind.METHOD, signature.getKind());
        assertEquals(expectedTopTypeFullName, signature.getTopTypeName());
        assertEquals(expectedTypeFullName, signature.getTypeCanonicalName());
        assertEquals(expectedFullName, signature.getCanonicalName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
    
    @Test
    public void testInnerTypeModifiers() {
        var signature = Signature.get("java.util.AbstractMap.SimpleEntry", null, resolveType);
        assertTrue(Modifier.isStatic(signature.getModifiers()));        
    }
    
    @Test
    public void testFieldModifiers() {
        var signature = Signature.get("String.CASE_INSENSITIVE_ORDER:Comparator", null, resolveType);
        assertTrue(Modifier.isStatic(signature.getModifiers()));        
    }
    
    @Test
    public void testEnumConstantModifiers() {
        var signature = Signature.get("java.lang.Thread.State.NEW", "State", resolveType);
        assertTrue(Modifier.isStatic(signature.getModifiers()));        
    }
    
    @Test
    public void testMethodModifiers() {
        var signature = Signature.get("String.valueOf(int i)", null, resolveType);
        assertTrue(Modifier.isStatic(signature.getModifiers()));        
    }
}
