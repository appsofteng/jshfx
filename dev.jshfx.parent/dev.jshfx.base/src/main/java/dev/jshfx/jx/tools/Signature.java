package dev.jshfx.jx.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Signature {

    private final String signature;
    private String topTypeFullName;
    private String typeFullName;
    private String fullName;
    private List<String> methodParameterTypes = new ArrayList<>();
    private BiFunction<String, Collection<String>, String> resolveFullTypeName;
    private Kind kind;

    private Signature(String signature, BiFunction<String, Collection<String>, String> resolveFullTypeName) {
        this.signature = signature;
        this.resolveFullTypeName = resolveFullTypeName;
    }

    public static Signature get(String signature, String expressionType,
            BiFunction<String, Collection<String>, String> resolveFullTypeName) {

        var instance = new Signature(signature, resolveFullTypeName);

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
            instance.typeFullName = signature;
            instance.fullName = signature;
        }

        instance.parseTopTypeFullName();

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

    public BiFunction<String, Collection<String>, String> getResolveFullTypeName() {
        return resolveFullTypeName;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return signature;
    }

    private void parseTopTypeFullName() {
        int i = 0;
        String enclosingType = typeFullName;
        topTypeFullName = typeFullName;

        while (i > -1 && enclosingType != null) {
            i = enclosingType.lastIndexOf(".");
            enclosingType = enclosingType.substring(0, i);
            enclosingType = resolveFullTypeName.apply(enclosingType, List.of());

            if (enclosingType != null) {
                topTypeFullName = enclosingType;
            }
        }
    }

    private void parseType(String type) {
        int i = type.lastIndexOf(".");

        if (i > 0) {
            typeFullName = type;
        } else {
            typeFullName = resolveFullTypeName.apply(type, List.of());
        }
    }

    private void parseField() {
        int i = signature.lastIndexOf(".");
        var fieldName = signature.substring(i + 1, signature.lastIndexOf(":"));
        var type = signature.substring(0, i);
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
        typeAndName = typeAndName.substring(typeAndName.lastIndexOf(" ") + 1);
        int i = typeAndName.lastIndexOf(".");
        var methodName = typeAndName.substring(i + 1);
        var type = typeAndName.substring(0, i);

        i = methodName.lastIndexOf(">");

        if (i > 0) {
            methodName = methodName.substring(i + 1);
        }

        parseType(type);

        fullName = typeFullName + "." + methodName;

        var parameters = signature.substring(p1 + 1, p2);
        parseMethodParameterTypes(parameters);

    }

    private void parseMethodParameterTypes(String parameters) {
        var typePattern = "(?:[\\w]+\\.)*[\\w]{2,}";
        var varArgsPattern = typePattern + "(?:\\.\\.\\.)";
        var typeOrArrayPattern = typePattern + "(?:\\[\\])?";

        String parameterPattern = String.format("(%s)|(%s)(?: +|<.*>)", varArgsPattern, typeOrArrayPattern);

        Pattern pattern = Pattern.compile(parameterPattern);
        Matcher matcher = pattern.matcher(parameters);

        while (matcher.find()) {
            String paramType = Stream.of(1, 2).map(i -> matcher.group(i)).filter(s -> s != null).findFirst().get();

            var noArrayType = paramType;
            var array = "";

            if (noArrayType.endsWith("[]")) {
                noArrayType = noArrayType.substring(0, noArrayType.indexOf("["));
                array = "[]";
            } else if (noArrayType.endsWith("...")) {
                noArrayType = noArrayType.substring(0, noArrayType.indexOf("."));
                array = "...";
            }

            int i = noArrayType.lastIndexOf(".");

            if (i > 0) {
                noArrayType = noArrayType.substring(i + 1);
            }

            var resolvedType = resolveFullTypeName.apply(noArrayType, List.of());

            if (resolvedType != null) {
                paramType = resolvedType + array;
            }

            methodParameterTypes.add(paramType);
        }
    }

    public enum Kind {
        ENUM_CONSTANT, FIELD, METHOD, TYPE
    }
}
