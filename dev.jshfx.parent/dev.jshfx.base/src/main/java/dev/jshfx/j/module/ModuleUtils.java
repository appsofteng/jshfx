package dev.jshfx.j.module;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class ModuleUtils {

    private ModuleUtils() {
    }

    public static String getModuleNames(Path dir) {
        String modules = "";

        try {
            List<Path> paths = Files.list(dir).toList();
            ModuleFinder mf = ModuleFinder.of(paths.toArray(new Path[] {}));

            modules = mf.findAll().stream().map(mr -> mr.descriptor().name()).collect(Collectors.joining(","));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modules;
    }
}
