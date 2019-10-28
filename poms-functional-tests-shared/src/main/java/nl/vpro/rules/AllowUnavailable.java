package nl.vpro.rules;


import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;

/**
 * @author Michiel Meeuwissen
 */
public class AllowUnavailable implements InvocationInterceptor {
    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext) throws Throwable {
        try {
            invocation.proceed();
        } catch (javax.ws.rs.ServiceUnavailableException | java.net.ConnectException se) {
            throw new TestAbortedException(invocationContext.toString() + ":" + se.getMessage() + " " + se.getMessage());
        }
     }

}
