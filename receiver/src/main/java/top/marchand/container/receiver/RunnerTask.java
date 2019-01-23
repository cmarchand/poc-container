/*
 * MPL 2.0
 */
package top.marchand.container.receiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import top.marchand.container.receiver.executionOrderModel.ParameterModel;

/**
 *
 * @author cmarchand
 */
public class RunnerTask implements Runnable {
    private final ClassLoader cl;
    private final String className;
    private final String[] args;
    
    public RunnerTask(final ClassLoader cl, final String className, final String[] args) {
        super();
        this.cl=cl;
        this.className=className;
        this.args=args;
    }

    @Override
    public void run() {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("Starting "+className);
        try {
            Class clazz = cl.loadClass(className);
//            String[] parameters = new String[order.getParameters().size()];
//            int i=0;
//            for(ParameterModel pm: order.getParameters().values()) {
//                String s = pm.getName()+"="+pm.getValue();
//                parameters[i++] = s;
//            }
            Method m = clazz.getMethod("main", String[].class);
            Object[] ps = new Object[] {args};
            Thread.currentThread().setContextClassLoader(cl);
            m.invoke(null, ps);
        } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            System.err.println("while running "+className);
            ex.printStackTrace(System.err);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
