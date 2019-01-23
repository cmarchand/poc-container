/*
 * MPL 2.0
 */
package top.marchand.container.proc1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 *
 * @author cmarchand
 */
public class Proc1Starter {
    
    private static final Logger LOGGER = LogManager.getLogger(Proc1Starter.class);
    
    public static void main(String[] args) {
        System.out.println("Starting Proc1Starter");
        new Proc1Starter().run();
    }
    
    private Proc1Starter() {
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
            LOGGER.info(clazz.getName()+" available in "+this.getClass().getName()+" classloader");
        } catch(Exception ex) {
            LOGGER.error(className+" is not available in "+this.getClass().getName()+" classloader");
//            ex.printStackTrace(System.err);
        }
    }
}
