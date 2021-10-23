package dev.jshfx.base.sys;

import java.util.Set;

import dev.jshfx.util.RepositoryUtils;

public final class RepositoryManager extends Manager {

    private static final RepositoryManager INSTANCE = new RepositoryManager();

    private RepositoryUtils repositoryUtils;

    private RepositoryManager() {
    }

    public static RepositoryManager get() {
        return INSTANCE;
    }

    @Override
    public void init() throws Exception {
        repositoryUtils = new RepositoryUtils();
        repositoryUtils.init();
    }

    public void resolve(String coords, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

       repositoryUtils.resolve(coords, classPaths, sourcePaths);
    }

    public void resolvePom(String pom, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

        repositoryUtils.resolvePom(pom, classPaths, sourcePaths);
    }
}
