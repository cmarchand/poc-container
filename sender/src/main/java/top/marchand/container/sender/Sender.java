/*
 * MPL 2.0
 */
package top.marchand.container.sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Un sender qui envoi des groupes de 5 messages
 * @author <a href="christophe@marchand.top">Christophe Marchand</a>
 */
public class Sender {
    private static final String[] TARGETS = {
        "top.marchand.container:proc1:1.00.00-SNAPSHOT"/*,
        "top.marchand.container:proc2:1.00.00-SNAPSHOT"*/
    };
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
        try {
            Sender instance = new Sender(jmsUrl, true);
            instance.start();
            instance.stop();
        } catch(IOException | NamingException | JMSException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
    private final QueueConnection qConnection;
    private final QueueSession session;
    private Queue queue;

    public Sender(final String jmsUrl, final boolean valid) throws IOException, NamingException, JMSException {
        super();
        Properties jmsProperties = new Properties();
        jmsProperties.load(Sender.class.getResourceAsStream("/top/marchand/container/common/jms.properties"));
        jmsProperties.put(Context.PROVIDER_URL, jmsUrl);
        Context ctx = new InitialContext(jmsProperties);
        QueueConnectionFactory qFactory = (QueueConnectionFactory)ctx.lookup("connectionFactory");
        qConnection = qFactory.createQueueConnection();
        session= qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    protected void start() throws JMSException, IOException {
        queue = session.createQueue("container");
        System.out.println("Démarrage de l'envoi de messages");
        for(String s: TARGETS) {
            sendMessage(queue, s);
        }
    }
    protected void stop() throws JMSException {
        session.close();
        qConnection.close();
    }
    protected void sendMessage(Queue queue, String artSpec) throws JMSException, IOException {
        QueueSender sender = session.createSender(queue);
        try {
            byte[] buffer = getXmlMessage(artSpec);
            sendMessage(sender, buffer);
            System.out.println("Message sent");
        } catch(XMLStreamException ex) {
            System.err.println("Not able to create XML message");
        } finally {
            sender.close();
        }
    }
    protected void sendMessage(QueueSender sender, byte[]  content) throws JMSException {
        BytesMessage tm = session.createBytesMessage();
        tm.writeBytes(content);
        sender.send(tm);
    }
    protected byte[] getXmlMessage(String artSpec) throws XMLStreamException, IOException {
        String[] artifact = splitArtSpec(artSpec);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(baos);
        writer.writeStartDocument();
        writer.writeStartElement("executionOrder");
        writer.writeStartElement("component");
        writeElement(writer, "groupId", artifact[0]);
        writeElement(writer, "artifactId", artifact[1]);
        writeElement(writer, "version", artifact[2]);
        writer.writeEndElement();
        writer.writeEndDocument();
        baos.flush(); baos.close();
        return baos.toByteArray();
    }
    
    private void writeElement(XMLStreamWriter writer, String elName, String value) throws XMLStreamException {
        writer.writeStartElement(elName);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }
    protected String[] splitArtSpec(String artSpec) {
        return artSpec.split(":");
    }
    private static void displaySyntax() {
        System.err.println(SYNTAX);
    }
    
    private static final String SYNTAX= "java "+Sender.class.getName()+" jmsUrl [true|false]\n" +
            "Where\n" +
            "\tjmsUrl is the JMS provider URL\n" +
            "\tsend valid or invalid XML content\n";
}
