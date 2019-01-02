/*
 * MPL 2.0
 */
package top.marchand.container.proc2;

/**
 *
 * @author cmarchand
 */
public class Proc2Starter {

    public static void main(String[] args) {
        System.out.println("Starting Proc2Starter");
        new Proc2Starter().run();
    }
    
    private Proc2Starter() {
        super();
    }
    
    protected void run() {
        checkClassExists("top.marchand.container.receiver.Receiver");
        checkClassExists("top.marchand.container.proc1.Proc1Starter");
        checkClassExists("top.marchand.container.proc2.Proc2Starter");
    }
    
    private void checkClassExists(String className) {
        try {
            Class clazz = Class.forName(className);
            System.err.println(className+" available in "+this.getClass().getName()+" classloader");
        } catch(Exception ex) {
            System.err.println(className+" is not available in "+this.getClass().getName()+" classloader");
        }
    }
}
