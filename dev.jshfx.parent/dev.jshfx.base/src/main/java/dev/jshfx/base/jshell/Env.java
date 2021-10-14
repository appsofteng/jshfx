package dev.jshfx.base.jshell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.bind.annotation.JsonbTransient;

public class Env implements Comparable<Env> {

    private String name;
    private Set<String> classPath = new HashSet<>();
    private Set<String> modulePath = new HashSet<>();
    private Set<String> addModules = new HashSet<>();
    private Set<String> moduleLocations = new HashSet<>();
    private Set<ExportItem> addExports = new HashSet<>();

    public Env() {
    }
    
    public Env(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<String> getClassPath() {
        return classPath;
    }
    
    public void setClassPath(Set<String> classPath) {
		this.classPath = classPath;
	}

    public Set<String> getModulePath() {
        return modulePath;
    }
    
    public void setModulePath(Set<String> modulePath) {
		this.modulePath = modulePath;
	}

    public Set<String> getAddModules() {
        return addModules;
    }
    
    public void setAddModules(Set<String> addModules) {
		this.addModules = addModules;
	}

    public Set<String> getModuleLocations() {
        return moduleLocations;
    }

    public void setModuleLocations(Set<String> moduleLocations) {
        this.moduleLocations = moduleLocations;
    }

    public Set<ExportItem> getAddExports() {
        return addExports;
    }
    
    public void setAddExports(Set<ExportItem> addExports) {
		this.addExports = addExports;
	}

    @JsonbTransient
    public String[] getOptions() {

        List<String> options = new ArrayList<>();

        if (!classPath.isEmpty()) {
            options.add("--class-path");
            options.add(classPath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (!modulePath.isEmpty()) {
            options.add("--module-path");
            options.add(modulePath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (!addModules.isEmpty()) {
            options.add("--add-modules");
            options.add(addModules.stream().collect(Collectors.joining(",")));
        }

        if (!addExports.isEmpty()) {
            addExports.forEach(e -> {
                options.add("--add-exports");
                options.add(e.toString());
            });
        }

        return options.toArray(new String[] {});
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Env o) {
        return name.compareTo(o.name);
    }
}
