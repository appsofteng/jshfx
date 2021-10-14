package dev.jshfx.base.sys;

import java.util.List;

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

    public void resolve(String coords, List<String> artifacts) throws Exception {

       repositoryUtils.resolve(coords, artifacts);
    }

    public void resolvePom(String pom, List<String> artifacts) throws Exception {

        repositoryUtils.resolvePom(pom, artifacts);
    }
}
