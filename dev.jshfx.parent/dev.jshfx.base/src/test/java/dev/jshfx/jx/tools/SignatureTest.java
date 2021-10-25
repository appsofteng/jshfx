package dev.jshfx.jx.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SignatureTest {

    @Test
    public void testTypeSimpleName() {
        String expectedTypeFullName = "java.lang.String";
        String expectedTypeSimpleName = "String";
        var signature = Signature.get(expectedTypeSimpleName, (type, imports) -> expectedTypeFullName);

        System.out.println(signature.getTypeFullName());
        System.out.println(signature.getTypeSimpleName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
    }

    @Test
    public void testTypeFullName() {
        String expectedTypeFullName = "java.lang.String";
        String expectedTypeSimpleName = "String";
        var signature = Signature.get(expectedTypeFullName, (type, imports) -> expectedTypeFullName);

        System.out.println(signature.getTypeFullName());
        System.out.println(signature.getTypeSimpleName());
        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
    }

    @Test
    public void testFieldName() {
        String expectedTypeFullName = "java.lang.Double";
        String expectedTypeSimpleName = "Double";
        String expectedFieldName = "BYTES";
        var signature = Signature.get("Double.BYTES:int", (type, imports) -> expectedTypeFullName);

        System.out.println(signature.getTypeFullName());
        System.out.println(signature.getTypeSimpleName());
        System.out.println(signature.getFieldName());

        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
        assertEquals(expectedFieldName, signature.getFieldName());
    }

    @Test
    public void testEnumLiteralName() {
        String expectedTypeFullName = "javax.lang.model.element.ElementKind";
        String expectedTypeSimpleName = "ElementKind";
        String expectedEnumLiteralName = "ANNOTATION_TYPE";

        var signature = Signature.get("ElementKind.ANNOTATION_TYPE",
                (type, imports) -> type.equals(expectedEnumLiteralName) ? null : expectedTypeFullName);

        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
        assertEquals(expectedEnumLiteralName, signature.getEnumLiteralName());
    }

    @Test
    public void testSimpleMethodName() {
        String expectedTypeFullName = "java.land.String";
        String expectedTypeSimpleName = "String";
        String expectedMethodName = "charAt";
        List<String> expectedMethodParamTypes = List.of("int");

        var signature = Signature.get("char String.charAt(int index)", (type, imports) -> type.equals(expectedTypeSimpleName) ? expectedTypeFullName : null);

        assertEquals(expectedTypeFullName, signature.getTypeFullName());
        assertEquals(expectedTypeSimpleName, signature.getTypeSimpleName());
        assertEquals(expectedMethodName, signature.getMethodName());
        assertEquals(expectedMethodParamTypes, signature.getMethodParameterTypes());
    }
}
