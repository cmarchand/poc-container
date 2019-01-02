/*
 * MPL 2.0
 */
package top.marchand.container.receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.sf.saxon.s9api.SaxonApiException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import top.marchand.container.receiver.aether.DependencyCrawler;
import top.marchand.container.receiver.aether.JarProvider;
import top.marchand.container.receiver.executionOrderModel.ExecutionOrderModel;
import top.marchand.container.receiver.executionOrderModel.MessageParser;
import top.marchand.container.receiver.executionOrderModel.ParameterModel;

/**
 *
 * @author <a href="christophe@marchand.top">Christophe Marchand</a>
 */
public class Receiver implements MessageListener {
    private final MessageParser parser;
    /**
     * Point d'entrée
     * @param args Paramètres de ligne de commande
     */
    public static void main(String[] args) {
        if(args.length<1) {
            displaySyntax();
            System.exit(-1);
        }
        String jmsUrl = args[0];
        String fail = args.length>1 ? args[1] : null;
        try {
            Receiver instance = new Receiver(jmsUrl, fail);
            instance.start();
        } catch(IOException | NamingException | JMSException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
    private final QueueConnection qConnection;
    private final QueueSession session;
    private QueueReceiver receiver;
    private final boolean fail;
    private boolean stop;
    
    public Receiver(String jmsUrl, String fail) throws IOException, NamingException, JMSException {
        super();
        Properties jmsProperties = new Properties();
        jmsProperties.load(Receiver.class.getResourceAsStream("/top/marchand/container/common/jms.properties"));
        jmsProperties.put(Context.PROVIDER_URL, jmsUrl);
        Context ctx = new InitialContext(jmsProperties);
        QueueConnectionFactory qFactory = (QueueConnectionFactory)ctx.lookup("connectionFactory");
        qConnection = qFactory.createQueueConnection();
        session= qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        this.fail = "fail".equals(fail);
        parser = new MessageParser();
    }
    
    protected void start() throws JMSException, IOException {
        Queue queue = session.createQueue("container");
        receiver = session.createReceiver(queue);
        receiver.setMessageListener(this);
        qConnection.start();
        InputStream is = System.in;
        System.out.println("Démarrage de la réception des messages");
        while(!stop) {
            if(is.available()!=0) {
                // on vide System.in pour ne pas perturber la console après l'execution
                while(is.available()>0) is.read();
                stop();
                break;
            }
        }
    }
    
    protected void stop() throws JMSException{
        stop=true;
        receiver.close();
        session.close();
        qConnection.stop();
        qConnection.close();
    }

    @Override
    public void onMessage(Message msg) {
        try {
            System.out.println("Message received: "+msg.getJMSMessageID());
            msg.acknowledge();
            BytesMessage bm = (BytesMessage)msg;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = bm.readBytes(buffer);
            while(read>0) {
                baos.write(buffer, 0, read);
                read = bm.readBytes(buffer);
            }
            ExecutionOrderModel executionOrder = parser.parseMessage(new ByteArrayInputStream(baos.toByteArray()));
            process(executionOrder);
        } catch(SaxonApiException | JMSException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    protected void process(ExecutionOrderModel order) {
        try {
            String localRepositoryPath = "/Users/cmarchand/.m2/repository";
            DependencyCrawler crawler = new DependencyCrawler(localRepositoryPath);
            Artifact processRootArtifact = new DefaultArtifact(
                    order.getComponent().getGroupId(), 
                    order.getComponent().getArtifactId(), 
                    "jar", 
                    order.getComponent().getVersion());
            // TODO : add environnement and business to classpath
            List<Artifact> processArtifacts = crawler.getRecursiveDependencies(processRootArtifact);
            JarProvider jarProvider = new JarProvider(localRepositoryPath);
            List<Artifact> resolvedProcessArtifacts = new ArrayList<>(processArtifacts.size());
            for(Artifact artifact: processArtifacts) {
                resolvedProcessArtifacts.add(jarProvider.getJar(artifact));
            }
            URL[] urls = new URL[resolvedProcessArtifacts.size()];
            for(int i=0;i<urls.length;i++) {
                urls[i] = resolvedProcessArtifacts.get(i).getFile().toURI().toURL();
            }
            System.out.println("  classpath contains "+urls.length+" entries");
            // eliminates these classes from classloader
            // maybe, we need to add some parts, especially for cp:/ protocol implementation
            ClassLoader processClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader().getParent());
            String startingClassName = getStartingClassName(resolvedProcessArtifacts.get(0).getFile());
            startProcess(processClassLoader, startingClassName, order);
        } catch(IOException | DependencyResolutionException | DependencyCollectionException | ArtifactResolutionException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    protected void startProcess(ClassLoader cl, String className, ExecutionOrderModel order) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            Class clazz = cl.loadClass(className);
            String[] parameters = new String[order.getParameters().size()];
            int i=0;
            for(ParameterModel pm: order.getParameters().values()) {
                String s = pm.getName()+"="+pm.getValue();
                parameters[i++] = s;
            }
            Method m = clazz.getMethod("main", parameters.getClass());
            Object[] ps = new Object[] {parameters};
            m.invoke(null, ps);
        } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            System.err.println("while running "+className);
            ex.printStackTrace(System.err);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    private String getStartingClassName(File jarFile) throws IllegalArgumentException {
        try {
            JarFile jf = new JarFile(jarFile);
            Manifest mf = jf.getManifest();
            return mf.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        } catch(IOException ex) {
            throw new IllegalArgumentException("Problem reading in "+jarFile.getAbsolutePath(), ex);
        }
    }
    
    private static void displaySyntax() {
        System.err.println(SYNTAX);
    }
    
    private static final String SYNTAX= "java "+Receiver.class.getName()+" jmsUrl\n" +
            "Where\n" +
            "\tjmsUrl is the JMS provider URL\n";
    
}
