package nl.vpro.junit.extensions;


import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
public class DoAfterException implements InvocationInterceptor {

    public DoAfterException() {
    }
    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext) throws Throwable {
        final Consumer<Throwable> job =
            ((WithJob) extensionContext.getRequiredTestInstance()).getJob();
        try {
            invocation.proceed();
        } catch (Throwable t) {
            if (t instanceof TestAbortedException) {
                log.warn(t.getMessage());
            } else {
                job.accept(t);
            }
            throw t;
        }
    }

    public interface WithJob {
        Consumer<Throwable> getJob();
    }
}
