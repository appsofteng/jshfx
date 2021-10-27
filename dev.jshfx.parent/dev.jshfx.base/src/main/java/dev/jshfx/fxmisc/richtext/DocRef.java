package dev.jshfx.fxmisc.richtext;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class DocRef {

    protected final String docCode;
    protected final String signature;

    public DocRef(String docCode) {
        this(docCode, "");
    }

    public DocRef(String docCode, String signature) {
        this.docCode = docCode;
        this.signature = signature;
    }

    public String getDocCode() {
        return docCode;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isUrl() {
        return getDocCode().matches("https?://.*");
    }

    @Override
    public String toString() {
        return new TreeMap<>(Map.of("docCode", docCode, "signature", signature)).toString();
    }
}
