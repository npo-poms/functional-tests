package nl.vpro.poms;

import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.extension.*;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
public class Require  implements InvocationInterceptor {

    public static final String NEEDED = "NEEDED";

    @SuppressWarnings("unchecked")
    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext) throws Throwable {

        Needs annotation = invocationContext.getExecutable().getAnnotation(Needs.class);
        if (annotation != null) {
            Set<String> neededObjects = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent(NEEDED, (k) -> new HashSet<String>(), Set.class);
            neededObjects.add(annotation.value());
            log.info("Needed objects {}", neededObjects);
        }
        invocation.proceed();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Needs {
        String value();
    }
}
