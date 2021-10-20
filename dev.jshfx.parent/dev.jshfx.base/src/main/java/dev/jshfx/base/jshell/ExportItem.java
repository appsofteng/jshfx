package dev.jshfx.base.jshell;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.bind.annotation.JsonbTransient;

public class ExportItem {

    private String sourceModule;
    private String packageName;
    private Set<String> targetModules = new HashSet<>();

    public ExportItem() {
    }

    public ExportItem(String sourceModule, String packageName, String targetModule) {
        this.sourceModule = sourceModule;
        this.packageName = packageName;
        targetModules.add(targetModule);
    }

    public ExportItem(String sourceModule, String packageName, Set<String> targetModules) {

        this.sourceModule = sourceModule;
        this.packageName = packageName;
        this.targetModules = targetModules;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Set<String> getTargetModules() {
        return targetModules;
    }

    public void setTargetModules(Set<String> targetModule) {
        this.targetModules = targetModule;
    }

    public static ExportItem parse(String input) {
        ExportItem item = new ExportItem();
        String[] parts = input.split("/");

        if (parts.length == 2) {
            item.setSourceModule(parts[0]);
            parts = parts[1].split("=");

            if (parts.length == 2) {
                item.setPackageName(parts[0]);
                parts = parts[1].split(",");
                if (parts.length > 0) {
                    item.getTargetModules().addAll(Arrays.asList(parts));
                } else {
                    throw new IllegalArgumentException(input);
                }
            } else {
                throw new IllegalArgumentException(input);
            }
        } else {
            throw new IllegalArgumentException(input);
        }

        return item;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof ExportItem other) {
            result = Objects.equals(getSource(), other.getSource());
        }
        
        return result;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getSource());
    }

    @JsonbTransient
    public String getSource() {
        return sourceModule + "/" + packageName;
    }
    
    @JsonbTransient
    public String getTarget() {
        return targetModules.stream().collect(Collectors.joining(","));
    }
    
    @Override
    public String toString() {
        return getSource() + "=" + getTarget();
    }
}
