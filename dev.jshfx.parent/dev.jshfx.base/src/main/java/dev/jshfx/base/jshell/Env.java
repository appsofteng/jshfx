package dev.jshfx.base.jshell;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.bind.annotation.JsonbTransient;

public class Env {

    private boolean load = true;
    private Set<String> sourcePaths = new LinkedHashSet<>();
    private Set<String> classPaths = new LinkedHashSet<>();
    private Set<String> modulePaths = new LinkedHashSet<>();
    private Set<String> addModules = new LinkedHashSet<>();
    private Set<ExportItem> addExports = new LinkedHashSet<>();

    public boolean isLoad() {
        return load;
    }

    public void setLoad(boolean load) {
        this.load = load;
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

    public void clear() {
        sourcePaths.clear();
        classPaths.clear();
        modulePaths.clear();
        addModules.clear();
        addExports.clear();
    }

    private List<String> getOptionList(String classpath, String modulepath, String modules) {

        List<String> options = new ArrayList<>();

        if (!classPaths.isEmpty() && load || !classpath.isEmpty()) {
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

        if (!modulePaths.isEmpty() && load || !modulepath.isEmpty()) {
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

        if (!addModules.isEmpty() && load || !modules.isEmpty()) {
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

        if (!addExports.isEmpty() && load) {
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
        return getOptionList("", "", "").stream().collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof Env env) {
            result = classPaths.equals(env.classPaths) && modulePaths.equals(env.modulePaths)
                    && addModules.equals(env.addModules) && addExports.equals(env.addExports);
        }

        return result;
    }
}
