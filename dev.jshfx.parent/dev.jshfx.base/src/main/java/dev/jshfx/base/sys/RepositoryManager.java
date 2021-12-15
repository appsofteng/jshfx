package dev.jshfx.base.sys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.jshfx.automatic.RepositoryUtils;

public final class RepositoryManager extends Manager {

    private static final RepositoryManager INSTANCE = new RepositoryManager();

    private static final Logger LOGGER = Logger.getLogger(RepositoryManager.class.getName());

    private RepositoryUtils repositoryUtils;

    private Set<Comparable<?>> repoCoordinates = new TreeSet<>();

    private ReentrantLock lock = new ReentrantLock();

    private RepositoryManager() {
    }

    public static RepositoryManager get() {
        return INSTANCE;
    }

    @Override
    public void init() throws Exception {
        repositoryUtils = new RepositoryUtils();
        repositoryUtils.init();
        loadRepoCoordinates();
    }

    public List<String> getRepoCoordinates() {
        lock.lock();
        try {
            return repoCoordinates.stream().map(c -> c.toString()).toList();
        } finally {
            lock.unlock();
        }
    }

    public Path getLocalRepoDir() {
        return repositoryUtils.getLocalRepoDir();
    }

    public void resolve(String coords, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

        repositoryUtils.resolve(coords, classPaths, sourcePaths, repoCoordinates);
    }

    public void resolvePom(String pom, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

        repositoryUtils.resolvePom(pom, classPaths, sourcePaths, repoCoordinates);
    }

    private void loadRepoCoordinates() {

        var repoDir = RepositoryManager.get().getLocalRepoDir();

        if (Files.exists(repoDir)) {

            TaskManager.get().execute(() -> {

                try {
                    var coordinates = Files.walk(repoDir)
                            .filter(p -> !Files.isDirectory(p) && !p.equals(repoDir) && p.toString().endsWith(".jar"))
                            .map(p -> repositoryUtils.toRepoCoordinates(repoDir, p.getParent())).sorted().distinct()
                            .toList();

                    lock.lock();
                    try {
                        repoCoordinates.addAll(coordinates);
                    } finally {
                        lock.unlock();
                    }

                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            });
        }
    }
}
