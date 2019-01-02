/*
 * MPL 2.0
 */
package top.marchand.container.receiver.executionOrderModel;

import java.io.InputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

/**
 *
 * @author cmarchand
 */
public class MessageParser {
    public static final QName QN_NAME = new QName("name");
    public static final QName QN_VALUE = new QName("value");
    private final Processor proc;
    private final DocumentBuilder builder;
    private final XPathCompiler compiler;
    
    public MessageParser() {
        this(new Processor(Configuration.newConfiguration()));
        
    }
    public MessageParser(Processor proc) {
        super();
        this.proc = proc;
        builder = proc.newDocumentBuilder();
        compiler = proc.newXPathCompiler();
    }
    public ExecutionOrderModel parseMessage(InputStream is) throws SaxonApiException {
        XdmNode document = builder.build(new StreamSource(is));
        XPathSelector compSelector = compiler.compile("/executionOrder/component").load();
        compSelector.setContextItem(document);
        
        TinyArtifact comp = createComponent((XdmNode)(compSelector.evaluateSingle()));
        ExecutionOrderModel ret = new ExecutionOrderModel(comp);
        
        XPathSelector envSelector = compiler.compile("/executionOrder/environnement").load();
        envSelector.setContextItem(document);
        XdmItem it = envSelector.evaluateSingle();
        if(it!=null) {
            ret.setEnvironnement(createComponent((XdmNode)it));
        }
        
        XPathSelector busSelector = compiler.compile("/executionOrder/business").load();
        busSelector.setContextItem(document);
        it = busSelector.evaluateSingle();
        if(it!=null) {
            ret.setBusiness(createComponent((XdmNode)it));
        }
        
        XPathSelector paramSelector = compiler.compile("/executionOrder/parameters/parameter").load();
        paramSelector.setContextItem(document);
        XdmSequenceIterator ite = paramSelector.evaluate().iterator();
        while(ite.hasNext()) {
            XdmNode pNode = (XdmNode)ite.next();
            ret.addParameter(createParameter(pNode));
        }
        return ret;
    }
    
    protected TinyArtifact createComponent(XdmNode node) throws SaxonApiException {
        String groupId=null;
        String artifactId=null;
        String version=null;
        XdmSequenceIterator it = node.axisIterator(Axis.CHILD);
        while(it.hasNext()) {
            XdmNode child = (XdmNode)it.next();
            if(XdmNodeKind.ELEMENT.equals(child.getNodeKind())) {
                String value = child.getStringValue();
                switch(child.getNodeName().getLocalName()) {
                    case "groupId": groupId = value; break;
                    case "artifactId": artifactId = value; break;
                    case "version": version = value; break;
                    default: throw new SaxonApiException("Unexpected element in "+node.getNodeName()+": "+child.getNodeName());
                }
            }
        }
        if(groupId==null || artifactId==null || version==null) {
            throw new SaxonApiException("following elements are required in "+node.getNodeName()+": groupId, artifactId, version");
        }
        return new TinyArtifact(groupId, artifactId, version);
    }
    
    protected ParameterModel createParameter(XdmNode node) throws SaxonApiException {
        String pName = node.getAttributeValue(QN_NAME);
        if(pName==null || pName.isEmpty()) {
            throw new SaxonApiException("<param must have a non-empty name attribute.");
        }
        String pValue = node.getAttributeValue(QN_VALUE);
        if(pValue==null) {
            pValue = node.getStringValue();
        }
        return new ParameterModel(pName, pValue);
    }
}
