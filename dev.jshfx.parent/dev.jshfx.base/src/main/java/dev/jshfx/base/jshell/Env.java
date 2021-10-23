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

    private List<String> getOptionList() {

        List<String> options = new ArrayList<>();

        if (!classPaths.isEmpty()) {
            options.add("--class-path");
            options.add(getClassPath());
        }

        if (!modulePaths.isEmpty()) {
            options.add("--module-path");
            options.add(getModulePath());
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

        return options;
    }

    @JsonbTransient
    public String[] getOptions() {

        return getOptionList().toArray(new String[] {});
    }

    @Override
    public String toString() {
        return name + "\n" + getOptionList().stream().collect(Collectors.joining(" "));
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
