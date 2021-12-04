package dev.jshfx.base.sys;

import java.nio.file.Path;
import java.util.Set;

import dev.jshfx.automatic.RepositoryUtils;

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

    public Path getLocalRepoDir() {
        return repositoryUtils.getLocalRepoDir();
    }
    
    public Comparable<?> toRepoCoordinates(Path repoDir, Path path) {
        return repositoryUtils.toRepoCoordinates(repoDir, path);
    }
    
    public void resolve(String coords, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

       repositoryUtils.resolve(coords, classPaths, sourcePaths);
    }

    public void resolvePom(String pom, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

        repositoryUtils.resolvePom(pom, classPaths, sourcePaths);
    }
}
