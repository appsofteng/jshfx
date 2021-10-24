package dev.jshfx.jx.tools;

import java.util.Collection;

public class Signature {

    private final String signature;
    private String fullName;
    private String simpleName;

    private Signature(String signature) {
        this.signature = signature;
    }
    
    
    public static Signature get(String signature, Collection<String> imports) {
        
        var instance = new Signature(signature);
        instance.fullName = signature;
        instance.simpleName = signature.substring(signature.lastIndexOf(".") + 1);
        
        return instance;
    }
        
    public String getFullName() {
        return fullName;
    }
    
    public String getSimpleName() {
        return simpleName;
    }
    
    @Override
    public String toString() {
        return signature;
    }
}
