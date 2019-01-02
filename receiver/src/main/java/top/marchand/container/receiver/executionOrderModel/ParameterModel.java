/*
 * MPL 2.0
 */
package top.marchand.container.receiver.executionOrderModel;

/**
 *
 * @author cmarchand
 */
public class ParameterModel {
    private final String name;
    private final String value;
    
    public ParameterModel(String name, String value) {
        super();
        this.name=name;
        this.value=value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    
}
