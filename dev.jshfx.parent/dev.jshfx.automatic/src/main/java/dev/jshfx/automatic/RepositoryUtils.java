package dev.jshfx.automatic;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

public final class RepositoryUtils {

    private static final Logger LOGGER = Logger.getLogger(RepositoryUtils.class.getName());
    private RepositorySystem system;
    private RepositorySystemSession session;
    private List<RemoteRepository> repositories;

    public void init() throws Exception {
        system = newRepositorySystem();
        session = newRepositorySystemSession(system);
        repositories = newRepositories(system, session);
    }

    public Path getLocalRepoDir() {
        return session.getLocalRepository().getBasedir().toPath();
    }

    public Comparable<?> toRepoCoordinates(Path repoDir, Path path) {
        ComparableVersion version = new ComparableVersion(path.getFileName().toString());
        Path parent = path.getParent();
        String artifact = parent.getFileName().toString();
        String group = repoDir.relativize(parent.getParent()).toString().replace(File.separatorChar, '.');

        return new Coordinates(group, artifact, version);
    }

    public void resolve(String coords, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

        Artifact artifact = new DefaultArtifact(coords);

        resolve(artifact, classPaths, sourcePaths);
    }

    public void resolvePom(String pom, Set<String> classPaths, Set<String> sourcePaths) throws Exception {

        File pomFile = new File(pom);
        ModelBuildingRequest modelBuildingRequest = new DefaultModelBuildingRequest().setProcessPlugins(false)
                .setPomFile(pomFile).setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();
        ModelBuildingResult modelBuildingResult = modelBuilder.build(modelBuildingRequest);

        Model model = modelBuildingResult.getEffectiveModel();

        for (org.apache.maven.model.Dependency dependency : model.getDependencies()) {
            Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getType(), dependency.getVersion());
            resolve(artifact, classPaths, sourcePaths);
        }
    }

    private void resolve(Artifact artifact, Set<String> classePaths, Set<String> sourcePaths)
            throws DependencyResolutionException, ArtifactResolutionException {
        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(repositories);

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        List<ArtifactResult> artifactResults = system.resolveDependencies(session, dependencyRequest)
                .getArtifactResults();

        for (var ar : artifactResults) {
            var classArtifact = ar.getArtifact();
            classePaths.add(classArtifact.getFile().toString());
            Artifact sourceArtifact = new DefaultArtifact(classArtifact.getGroupId(), classArtifact.getArtifactId(),
                    "sources", classArtifact.getExtension(), classArtifact.getVersion());
            sourceArtifact = resolve(sourceArtifact);
            if (sourceArtifact != null) {
                sourcePaths.add(sourceArtifact.getFile().toString());
            }
        }
    }

    private Artifact resolve(Artifact artifact) {

        Artifact result = null;

        try {
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(repositories);

            ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);

            result = artifactResult.getArtifact();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }

        return result;
    }

    private RepositorySystem newRepositorySystem() {

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                System.err.format("Service creation failed for %s with implementation %s, exception %s", type, impl,
                        exception);
            }
        });

        return locator.getService(RepositorySystem.class);
    }

    private DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(null);
        session.setRepositoryListener(null);

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    private List<RemoteRepository> newRepositories(RepositorySystem system, RepositorySystemSession session) {

        var central = new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/")
                .build();
        var sonatype = new RemoteRepository.Builder("oss.sonatype.org-snapshot", "",
                "http://oss.sonatype.org/content/repositories/snapshots")
                        .setReleasePolicy(new RepositoryPolicy(false, null, null))
                        .setSnapshotPolicy(new RepositoryPolicy(true, null, null)).build();

        return List.of(central, sonatype);
    }
    
    private record Coordinates(String group, String artifact, ComparableVersion version) implements Comparable<Coordinates> {

        @Override
        public int compareTo(Coordinates o) {
            int result = group.compareTo(o.group);
            
            if (result == 0) {
                result = artifact.compareTo(o.artifact);
                
                if (result == 0) {
                    result = version.compareTo(o.version);
                }
            }
            
            return result;
        }
        
        public String toString() {
            return String.format("%s:%s:%s", group, artifact, version);
        }
    }
}
