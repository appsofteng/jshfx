package dev.jshfx.base.jshell.commands;

import java.util.Iterator;

import dev.jshfx.base.sys.RepositoryManager;

public class RepoCoordinates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return RepositoryManager.get().getRepoCoordinates().iterator();
    }

}
