package dev.jshfx.jx.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ClassUtils;

import dev.jshfx.j.util.LU;

public class Signature {

    private static final Logger LOGGER = Logger.getLogger(Signature.class.getName());

    private final String signature;
    private String topTypeName;
    private String typeName;
    private String typeCanonicalName;
    private String canonicalName;
    private String simpleName;
    private List<String> methodParameterTypes = new ArrayList<>();
    private Function<String, String> resolveTypeName;
    private Kind kind;
    private Integer modifiers;

    private Signature(String signature, Function<String, String> resolveTypeName) {
        this.signature = signature;
        this.resolveTypeName = resolveTypeName;
    }

    public static Signature get(String signature, String expressionType, Function<String, String> resolveFullTypeName) {

        var instance = new Signature(signature, resolveFullTypeName);

        if (signature != null && !signature.isEmpty()) {

            if (signature.contains(":")) {
                var name = signature.substring(0, signature.lastIndexOf(":"));
                if (name.contains(".")) {
                    instance.kind = Kind.FIELD;
                    instance.parseField();
                } else {
                    instance.kind = Kind.VAR;
                    instance.canonicalName = name;
                    instance.simpleName = name;
                    instance.topTypeName = "";
                }
            } else if (signature.contains("(")) {
                instance.kind = Kind.METHOD;
                instance.parseMethod();
            } else if (expressionType != null) {
                instance.kind = Kind.ENUM_CONSTANT;
                instance.parseEnumConstant();
            } else {
                instance.kind = Kind.TYPE;
                instance.parseType(signature);
                instance.canonicalName = instance.typeCanonicalName;
                int i = instance.typeCanonicalName.lastIndexOf('.');
                instance.simpleName = i > -1 ? instance.typeCanonicalName.substring(i + 1) : instance.typeCanonicalName;
            }

            if (instance.topTypeName == null) {
                instance.setTopTypeName();
            }
        }

        return instance;
    }

    public String getTopTypeName() {
        return topTypeName;
    }

    public String getTypeCanonicalName() {
        return typeCanonicalName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }
    
    public String getSimpleName() {
        return simpleName;
    }

    public List<String> getMethodParameterTypes() {
        return methodParameterTypes;
    }

    public Kind getKind() {
        return kind;
    }

    public int getModifiers() {

        if (modifiers == null) {
            modifiers = 0;

            try {
                if (kind == Kind.TYPE) {
                    var type = Class.forName(typeName);
                    modifiers = type.getModifiers();
                } else if (kind == Kind.FIELD || kind == Kind.ENUM_CONSTANT) {
                    var type = Class.forName(typeName);
                    var field = type.getField(simpleName);
                    modifiers = field.getModifiers();
                } else if (kind == Kind.METHOD) {
                    var type = Class.forName(typeName);
                    Class<?>[] pramTypes = methodParameterTypes.stream().map(t -> LU.of(() -> ClassUtils.getClass(t)))
                            .toArray(Class[]::new);
                    var method = type.getMethod(simpleName, pramTypes);
                    modifiers = method.getModifiers();
                }

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, info(), e);
            }
        }

        return modifiers;
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
        return String.format(
                "Signature: %s, topTypeName: %s, typeName: %s, typeCanonicalName: %s, canonicalName: %s, simpleName: %s, kind: %s, methodParameterTypes: %s",
                signature, topTypeName, typeName, typeCanonicalName, canonicalName, simpleName, kind,
                methodParameterTypes);
    }

    private void setTopTypeName() {
        String enclosingType = typeCanonicalName;
        topTypeName = typeCanonicalName;
        int i = enclosingType.lastIndexOf(".");
        int lastDot = -1;

        while (i > -1 && enclosingType != null) {
            enclosingType = enclosingType.substring(0, i);
            enclosingType = resolveTypeName.apply(enclosingType);

            if (enclosingType != null) {
                topTypeName = enclosingType;
                lastDot = i;
                i = enclosingType.lastIndexOf(".");
            }
        }

        topTypeName = topTypeName.replaceAll("<.*>", "");
        typeName = lastDot > -1 ? topTypeName + typeCanonicalName.substring(lastDot).replace('.', '$')
                : typeCanonicalName;
    }

    private void parseType(String type) {
        int i = type.lastIndexOf(".");
        type = type.replaceAll("<.*>", "");

        if (i > 0) {
            typeCanonicalName = type;
        } else {
            typeCanonicalName = resolveTypeName.apply(type);
            typeCanonicalName = typeCanonicalName.replaceAll("<.*>", "");
        }
    }

    private void parseField() {
        var typeFieldName = signature.substring(0, signature.lastIndexOf(":"));
        int i = typeFieldName.lastIndexOf(".");
        simpleName = typeFieldName.substring(i + 1);
        var type = typeFieldName.substring(0, i);
        parseType(type);
        canonicalName = typeCanonicalName + "." + simpleName;
    }

    private void parseEnumConstant() {
        int i = signature.lastIndexOf(".");
        simpleName = signature.substring(i + 1);
        var type = signature.substring(0, i);
        parseType(type);
        canonicalName = typeCanonicalName + "." + simpleName;
    }

    private void parseMethod() {
        int p1 = signature.indexOf("(");
        int p2 = signature.lastIndexOf(")");
        var returnTypeAndName = signature.substring(0, p1);
        returnTypeAndName = removeBracketContent(returnTypeAndName);
        int i = returnTypeAndName.lastIndexOf(" ");
        var typeAndName = returnTypeAndName.substring(i + 1);
        String returnType = i > -1 ? returnTypeAndName.substring(0, i) : "";

        i = typeAndName.lastIndexOf(".");
        simpleName = typeAndName;
        String type = typeAndName;

        if (i > -1) {
            simpleName = typeAndName.substring(i + 1);
            type = typeAndName.substring(0, i);
            i = simpleName.lastIndexOf(">");

            if (i > 0) {
                simpleName = simpleName.substring(i + 1);
            }
            // Constructor e.g. URI(String u)
        } else if (returnType.isEmpty()) {
            simpleName = "<init>";
            // Method in JSH script.
        } else {
            type = "";
            typeCanonicalName = "";
            topTypeName = "";
        }

        if (!type.isEmpty()) {
            parseType(type);
        }

        canonicalName = typeCanonicalName + "." + simpleName;

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
            } else if (c == ' ' || c == ']'
                    || i > 1 && c == '.' && parameters.charAt(i - 1) == '.' && parameters.charAt(i - 2) == '.') {
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

        if (noArrayType.endsWith("]")) {
            int i = noArrayType.indexOf("[");
            array = noArrayType.substring(i);
            noArrayType = noArrayType.substring(0, i);
        } else if (noArrayType.endsWith("...")) {
            noArrayType = noArrayType.substring(0, noArrayType.indexOf("..."));
            array = "[]";
        }

        int i = noArrayType.lastIndexOf(".");

        if (i == -1) {
            var resolvedType = resolveTypeName.apply(noArrayType);
            if (resolvedType != null) {
                noArrayType = resolvedType.replaceAll("<.*>", "");
            }
        }

        paramType = noArrayType + array;

        methodParameterTypes.add(paramType);
    }

    public enum Kind {
        ENUM_CONSTANT, FIELD, METHOD, TYPE, VAR
    }
}
