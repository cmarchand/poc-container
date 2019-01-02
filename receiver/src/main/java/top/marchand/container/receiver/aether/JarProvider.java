/*
 * MPL 2.0
 */
package top.marchand.container.receiver.aether;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 *
 * @author cmarchand
 */
public class JarProvider extends AbstractAetherUtils {
    
    public JarProvider(String localRepoPath) {
        super(localRepoPath);
    }
    /**
     * Returns the file that resolves the artifact
     * @param artifact The artifact
     * @return The resolved Artifac. It may be different from artifact used as parameter.
     * @throws ArtifactResolutionException If artifact can not be found
     */
    public Artifact getJar(final Artifact artifact) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(repositories);
//        System.out.println("request context: "+request.getRequestContext());
//        System.out.println("session.localRepositoryManager: "+session.getLocalRepositoryManager());
        ArtifactResult result = system.resolveArtifact(session, request);
        return result.getArtifact();
    }

}
