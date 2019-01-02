/*
 * MPL 2.0
 */
package top.marchand.container.receiver.executionOrderModel;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cmarchand
 */
public class ExecutionOrderModel {
    
    private final TinyArtifact component;
    private TinyArtifact environnement;
    private TinyArtifact business;
    
    private final Map<String, ParameterModel> parameters;
    
    public ExecutionOrderModel(TinyArtifact component) {
        super();
        this.component=component;
        parameters = new HashMap<>();
    }
    
    public void addParameter(ParameterModel p) {
        parameters.put(p.getName(), p);
    }
    
    public String getParameterValue(String name) {
        ParameterModel p = parameters.get(name);
        if(p!=null) return p.getValue();
        else return null;
    }

    public TinyArtifact getComponent() {
        return component;
    }

    public TinyArtifact getEnvironnement() {
        return environnement;
    }

    public TinyArtifact getBusiness() {
        return business;
    }

    public Map<String, ParameterModel> getParameters() {
        return parameters;
    }

    public void setEnvironnement(TinyArtifact environnement) {
        this.environnement = environnement;
    }

    public void setBusiness(TinyArtifact business) {
        this.business = business;
    }
    
}
