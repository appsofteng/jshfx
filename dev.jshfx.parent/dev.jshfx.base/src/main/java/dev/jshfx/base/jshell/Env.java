package dev.jshfx.base.jshell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jshfx.base.sys.PreferenceManager;
import jakarta.json.bind.annotation.JsonbTransient;

public class Env implements Comparable<Env> {

    private String name = PreferenceManager.DEFAULT_ENV_NAME;
    private Set<String> sourcePaths = new HashSet<>();
    private Set<String> classPaths = new HashSet<>();
    private Set<String> modulePaths = new HashSet<>();
    private Set<String> addModules = new HashSet<>();
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

    public Set<String> getSourcePaths() {
        return sourcePaths;
    }
    
    public void setSourcePaths(Set<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }
    
    public Set<String> getClassPaths() {
        return classPaths;
    }

    public void setClassPaths(Set<String> classPath) {
        this.classPaths = classPath;
    }

    public Set<String> getModulePaths() {
        return modulePaths;
    }

    public void setModulePaths(Set<String> modulePath) {
        this.modulePaths = modulePath;
    }

    public Set<String> getAddModules() {
        return addModules;
    }

    public void setAddModules(Set<String> addModules) {
        this.addModules = addModules;
    }

    public Set<ExportItem> getAddExports() {
        return addExports;
    }

    public void setAddExports(Set<ExportItem> addExports) {
        this.addExports = addExports;
    }

    @JsonbTransient
    public String getClassPath() {
        return classPaths.stream().collect(Collectors.joining(File.pathSeparator));
    }

    @JsonbTransient
    public String getModulePath() {
        return modulePaths.stream().collect(Collectors.joining(File.pathSeparator));
    }

    private List<String> getOptionList(String classpath, String modulepath, String modules) {

        List<String> options = new ArrayList<>();

        if (!classPaths.isEmpty() || !classpath.isEmpty()) {
            options.add("--class-path");
            String path = getClassPath();
            
            if (path.isEmpty()) {
                path = classpath;
            } else {
                if (!classpath.isEmpty()) {
                    path += File.pathSeparator + classpath;
                }
            }
            options.add(path);
        }

        if (!modulePaths.isEmpty() || !modulepath.isEmpty()) {
            options.add("--module-path");
            String path = getModulePath();
            
            if (path.isEmpty()) {
                path = modulepath;
            } else {
                if (!modulepath.isEmpty()) {
                    path += File.pathSeparator + modulepath;
                }
            }
            options.add(path);
        }

        if (!addModules.isEmpty() || !modules.isEmpty()) {
            options.add("--add-modules");
            
            String addm = addModules.stream().collect(Collectors.joining(","));
            
            if (addm.isEmpty()) {
                addm = modules;
            } else {
                if (!modules.isEmpty()) {
                    addm += "," + modules;
                }
            }
            
            options.add(addm);
        }

        if (!addExports.isEmpty()) {
            addExports.forEach(e -> {
                options.add("--add-exports");
                options.add(e.toString());
            });
        }
        
        return options;
    }

    @JsonbTransient
    public String[] getOptions(String classpath, String modulepath, String modules, List<String> additionalOptions) {
        var options = getOptionList(classpath, modulepath, modules);
        options.addAll(additionalOptions);

        return options.toArray(new String[] {});
    }

    @Override
    public String toString() {
        return name + "\n" + getOptionList("", "", "").stream().collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof Env env) {
            result = name.equals(env.name) && classPaths.equals(env.classPaths) && modulePaths.equals(env.modulePaths)
                    && addModules.equals(env.addModules) && addExports.equals(env.addExports);
        }

        return result;
    }

    @Override
    public int compareTo(Env o) {
        return name.compareTo(o.name);
    }
}
