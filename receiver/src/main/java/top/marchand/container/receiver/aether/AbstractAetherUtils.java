/*
 * MPL 2.0
 */
package top.marchand.container.receiver.aether;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;

/**
 *
 * @author cmarchand
 */
public class AbstractAetherUtils {
    protected LocalRepository localRepo = null;
    protected RepositorySystem system = null;
    protected RepositorySystemSession session = null;
    protected List<RemoteRepository> repositories = new ArrayList<>();
    
    public AbstractAetherUtils(String localRepoPath) {
        super();
        localRepo = new LocalRepository( localRepoPath );
        system = newRepositorySystem();
        session = newSession(system);
        repositories.add((new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/")).build());
    }
    
    protected RepositorySystemSession newSession( RepositorySystem system ) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );
        DependencySelector depFilter = new AndDependencySelector(
//                new ScopeDependencySelector("provided" ),
                new OptionalDependencySelector(),
                new ExclusionDependencySelector()
                );
        session.setDependencySelector(depFilter);
        session.setOffline(true);
        return session;
    }
    protected RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
        return locator.getService( RepositorySystem.class );
    }
}
