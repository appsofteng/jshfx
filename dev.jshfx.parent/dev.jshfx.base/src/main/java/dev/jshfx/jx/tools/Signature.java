package dev.jshfx.jx.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Signature {

    private final String signature;
    private String topTypeFullName;
    private String typeFullName;
    private String fullName;
    private List<String> methodParameterTypes = new ArrayList<>();
    private Function<String, String> resolveFullTypeName;
    private Kind kind;

    private Signature(String signature, Function<String, String> resolveFullTypeName) {
        this.signature = signature;
        this.resolveFullTypeName = resolveFullTypeName;
    }

    public static Signature get(String signature, String expressionType, Function<String, String> resolveFullTypeName) {

        var instance = new Signature(signature, resolveFullTypeName);

        if (signature != null && !signature.isEmpty()) {

            if (signature.contains(":")) {
                instance.kind = Kind.FIELD;
                instance.parseField();
            } else if (signature.contains("(")) {
                instance.kind = Kind.METHOD;
                instance.parseMethod();
            } else if (expressionType != null) {
                instance.kind = Kind.ENUM_CONSTANT;
                instance.parseEnumConstant();
            } else {
                instance.kind = Kind.TYPE;
                instance.parseType(signature);
                instance.fullName = instance.typeFullName;
            }

            instance.parseTopTypeFullName();
        }

        return instance;
    }

    public String getTopTypeFullName() {
        return topTypeFullName;
    }

    public String getTypeFullName() {
        return typeFullName;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getMethodParameterTypes() {
        return methodParameterTypes;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Signature other) {
            result = Objects.equals(signature, other.signature);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature);
    }

    @Override
    public String toString() {
        return signature;
    }
    
    public String info() {
        return String.format("Signature: %s, topTypeFullName: %s, typeFullName: %s, fullName: %s, kind: %s", signature, topTypeFullName, typeFullName, fullName, kind);
    }

    private void parseTopTypeFullName() {
        int i = 0;
        String enclosingType = typeFullName;
        topTypeFullName = typeFullName;

        while (i > -1 && enclosingType != null) {
            i = enclosingType.lastIndexOf(".");
            enclosingType = enclosingType.substring(0, i);
            enclosingType = resolveFullTypeName.apply(enclosingType);

            if (enclosingType != null) {
                topTypeFullName = enclosingType;
            }
        }

        topTypeFullName = topTypeFullName.replaceAll("<.*>", "");
    }

    private void parseType(String type) {
        int i = type.lastIndexOf(".");
        type = type.replaceAll("<.*>", "");

        if (i > 0) {
            typeFullName = type;
        } else {
            typeFullName = resolveFullTypeName.apply(type);
            typeFullName = typeFullName.replaceAll("<.*>", "");
        }
    }

    private void parseField() {
        var typeFieldName = signature.substring(0, signature.lastIndexOf(":"));
        int i = typeFieldName.lastIndexOf(".");
        var fieldName = typeFieldName.substring(i + 1);
        var type = typeFieldName.substring(0, i);
        parseType(type);
        fullName = typeFullName + "." + fieldName;
    }

    private void parseEnumConstant() {
        int i = signature.lastIndexOf(".");
        var constName = signature.substring(i + 1);
        var type = signature.substring(0, i);
        parseType(type);
        fullName = typeFullName + "." + constName;
    }

    private void parseMethod() {
        int p1 = signature.indexOf("(");
        int p2 = signature.lastIndexOf(")");
        var typeAndName = signature.substring(0, p1);
        typeAndName = removeBracketContent(typeAndName);
        typeAndName = typeAndName.substring(typeAndName.lastIndexOf(" ") + 1);
        int i = typeAndName.lastIndexOf(".");
        String methodName = typeAndName;
        String type = typeAndName;

        // Constructor will no pass, e.g. URI(String u)
        if (i > -1) {
            methodName = typeAndName.substring(i + 1);
            type = typeAndName.substring(0, i);
            i = methodName.lastIndexOf(">");

            if (i > 0) {
                methodName = methodName.substring(i + 1);
            }
        } else {
            methodName = "<init>";
        }

        parseType(type);

        fullName = typeFullName + "." + methodName;

        var parameters = signature.substring(p1 + 1, p2);
        parseMethodParameterTypes(parameters);

    }

    private String removeBracketContent(String input) {
        String result = "";
        int delimiters = 0;

        for (int i = 0; i < input.length(); i++) {

            if (input.charAt(i) == '<' || input.charAt(i) == '[') {
                delimiters++;
            } else if (input.charAt(i) == '>' || input.charAt(i) == ']') {
                delimiters--;
            } else if (delimiters == 0) {
                result += input.charAt(i);
            } 
        }

        return result;
    }

    private void parseMethodParameterTypes(String parameters) {

        int genericDelimiters = 0;
        int typeStartIndex = 0;
        int typeEndIndex = 0;

        for (int i = 0; i < parameters.length(); i++) {
            char c = parameters.charAt(i);
            if (c == '<') {
                genericDelimiters++;
            } else if (c == '>') {
                genericDelimiters--;
                typeEndIndex = i;
            } else if (c == ' ' || c == ']' || c == '.') {
                typeEndIndex = i;
            }

            if (c == ',' && genericDelimiters == 0 || i == parameters.length() - 1) {
                if (typeEndIndex == typeStartIndex) {
                    typeEndIndex = c == ',' ? i - 1 : i;
                }
                var type = parameters.substring(typeStartIndex, typeEndIndex + 1).trim();
                parseMethodParameterType(type);
                typeStartIndex = i + 1;
                typeEndIndex = typeStartIndex;
            }
        }
    }

    private void parseMethodParameterType(String paramType) {

        var noArrayType = paramType.replaceAll("<.*>", "");
        var array = "";

        if (noArrayType.endsWith("[]")) {
            noArrayType = noArrayType.substring(0, noArrayType.indexOf("["));
            array = "[]";
        } else if (noArrayType.endsWith("...")) {
            noArrayType = noArrayType.substring(0, noArrayType.indexOf("."));
            array = "...";
        }

        int i = noArrayType.lastIndexOf(".");

        if (i == -1) {
            var resolvedType = resolveFullTypeName.apply(noArrayType);
            if (resolvedType != null) {
                noArrayType = resolvedType;
            }
        }

        paramType = noArrayType + array;

        methodParameterTypes.add(paramType);
    }

    public enum Kind {
        ENUM_CONSTANT, FIELD, METHOD, TYPE
    }
}
