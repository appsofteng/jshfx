package dev.jshfx.base.jshell.commands;

import java.util.Iterator;

import dev.jshfx.base.sys.FileManager;

public class RepoCoordinates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return FileManager.get().getRepoCoordinates().iterator();
    }

}
