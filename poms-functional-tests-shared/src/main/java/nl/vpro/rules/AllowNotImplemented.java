package nl.vpro.rules;


import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;


/**
 * @author Michiel Meeuwissen
 */
public class AllowNotImplemented implements InvocationInterceptor {
    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
        try {
            invocation.proceed();
        } catch (javax.ws.rs.ServerErrorException jse) {
            if (jse.getMessage().contains("HTTP 501")) {
                throw new TestAbortedException(invocationContext.toString() + ":" + jse.getMessage() + " " + jse.getMessage());
            } else {
                throw jse;
            }
        }

	}

}
