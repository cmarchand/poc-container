/*
 * MPL 2.0
 */
package top.marchand.container.receiver.executionOrderModel;

import java.net.URL;

/**
 *
 * @author cmarchand
 */
public class TinyArtifact {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private URL file;
    
    public TinyArtifact(String groupId, String artifactId, String version) {
        super();
        this.groupId=groupId;
        this.artifactId=artifactId;
        this.version=version;
    }

    public URL getFile() {
        return file;
    }

    public void setFile(URL file) {
        this.file = file;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
    
    
}
