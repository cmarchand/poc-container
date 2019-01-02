/*
 * MPL 2.0
 */
package top.marchand.container.receiver.aether;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.visitor.FilteringDependencyVisitor;
import org.eclipse.aether.util.graph.visitor.TreeDependencyVisitor;

/**
 * Copied and adapted from https://pastebin.com/5dGADZX0
 * @author cmarchand
 */
public class DependencyCrawler extends AbstractAetherUtils {
    
    
    public DependencyCrawler(String localRepoPath) throws IOException {
        super(localRepoPath);
    }

    public List<Artifact> getRecursiveDependencies(Artifact artifact) throws DependencyResolutionException, DependencyCollectionException {
        RepositorySystem system = newRepositorySystem();
        RepositorySystemSession session = newSession(system);
        
        List<Artifact> ret = new ArrayList<>();

//        artifact = new DefaultArtifact("org.aroundthecode.pathfinder:pathfinder-server:0.1.0-SNAPSHOT");

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE); // was TEST

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(repositories);

        //use collectDependencies to collect
        CollectResult collectResult = system.collectDependencies(session, collectRequest);
        DependencyNode node = collectResult.getRoot();
        node.accept(
                new TreeDependencyVisitor(
                        new FilteringDependencyVisitor(
                                new DependencyVisitor() {
			String indent = "";
			@Override
			public boolean visitEnter(DependencyNode dependencyNode) {
//				System.out.println(indent + dependencyNode.getArtifact());
				indent += "    ";
                                ret.add(dependencyNode.getArtifact());
				return true;
			}

			@Override
			public boolean visitLeave(DependencyNode dependencyNode) {
				indent = indent.substring(0, indent.length() - 4);
				return true;
			}
		}, classpathFlter)));
        return ret;
    }
    
}
