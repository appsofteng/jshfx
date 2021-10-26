package dev.jshfx.jx.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signature {

    private final String signature;
    private String typeFullName;
    private String typeSimpleName;
    private String methodName;
    private List<String> methodParameterTypes = new ArrayList<>();
    private String fieldName;
    private String enumLiteralName;
    private BiFunction<String, Collection<String>, String> resolveFullTypeName;

    private Signature(String signature, BiFunction<String, Collection<String>, String> resolveFullTypeName) {
        this.signature = signature;
        this.resolveFullTypeName = resolveFullTypeName;
    }

    public static Signature get(String signature, BiFunction<String, Collection<String>, String> resolveFullTypeName) {

        var instance = new Signature(signature, resolveFullTypeName);

        if (signature.contains(":")) {
            instance.parseField();
        } else if (signature.contains("(")) {
            instance.parseMethod();
        } else {
            int i = signature.lastIndexOf(".");

            if (i > 0) {
                var startPart = signature.substring(0, i);
                var endPart = signature.substring(i + 1);
                var resolvedType = resolveFullTypeName.apply(endPart, List.of());
                
                if (resolvedType == null) {
                    instance.enumLiteralName = endPart; 
                    instance.parseType(startPart);
                } else {
                    var nameSpace = resolvedType.substring(0, resolvedType.lastIndexOf("."));
                    if (nameSpace.endsWith(startPart)) {
                        instance.typeSimpleName = endPart;
                        instance.typeFullName = resolvedType;   
                    } else {
                        // Enum literal has the same name as a type which is different from the actual enum type.
                        instance.enumLiteralName = endPart; 
                        instance.parseType(startPart);
                    }
                }
            } else {
                instance.parseType();
            }
        }

        return instance;
    }

    public String getTypeFullName() {
        return typeFullName;
    }

    public String getTypeSimpleName() {
        return typeSimpleName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getMethodParameterTypes() {
        return methodParameterTypes;
    }

    public String getEnumLiteralName() {
        return enumLiteralName;
    }

    public BiFunction<String, Collection<String>, String> getResolveFullTypeName() {
        return resolveFullTypeName;
    }

    @Override
    public String toString() {
        return signature;
    }

    private void parseType() {
        parseType(signature);
    }

    private void parseType(String type) {
        int i = type.lastIndexOf(".");

        if (i > 0) {
            typeFullName = type;
            typeSimpleName = type.substring(i + 1);
        } else {
            typeSimpleName = type;
            var resolvedType = resolveFullTypeName.apply(type, List.of());
            typeFullName = resolvedType != null ? resolvedType : type;
        }
    }

    private void parseField() {
        int i = signature.lastIndexOf(".");
        fieldName = signature.substring(i + 1, signature.lastIndexOf(":"));
        var type = signature.substring(0, i);
        parseType(type);
    }

    private void parseMethod() {
        int p1 = signature.indexOf("(");
        int p2 = signature.lastIndexOf(")");
        var typeAndName = signature.substring(0, p1);
        typeAndName = typeAndName.substring(typeAndName.lastIndexOf(" ") + 1);
        int i = typeAndName.lastIndexOf(".");
        methodName = typeAndName.substring(i + 1);
        var type = typeAndName.substring(0, i);
        parseType(type);

        var parameters = signature.substring(p1 + 1, p2);
        parseMethodParameterTypes(parameters);

    }

    private void parseMethodParameterTypes(String parameters) {
        String parameterPattern = "([\\w\\.]{2,}(?:\\.\\.\\.|\\[\\])?)(?: +|<.*>)";
      
        Pattern pattern = Pattern.compile(parameterPattern);
        Matcher matcher = pattern.matcher(parameters);

        while (matcher.find()) {
            String paramType = matcher.group(1);

            int i = paramType.lastIndexOf(".");

            if (i > 0) {
                paramType = paramType.substring(i + 1);
            }

            var noArrayType = paramType;
            
            if (noArrayType.endsWith("[]")) {
                noArrayType = noArrayType.substring(0, noArrayType.indexOf("["));
            } else if (noArrayType.endsWith("...")) {
                noArrayType = noArrayType.substring(0, noArrayType.indexOf("."));
            }
            
            var resolvedType = resolveFullTypeName.apply(noArrayType, List.of());
            
            if (resolvedType != null) {
                paramType = resolvedType.substring(0, resolvedType.lastIndexOf(".") + 1) + paramType;
            }
            
            methodParameterTypes.add(paramType);
        }
    }
}
