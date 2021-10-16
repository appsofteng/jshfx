package dev.jshfx.base.jshell.commands;

import java.util.Iterator;

import dev.jshfx.base.sys.FileManager;

public class EnvNames implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return FileManager.get().getEnvNames().iterator();
    }

}
