package nl.vpro.poms;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
public class Require  implements InvocationInterceptor {

    public static final String NEEDED = "NEEDED";

    public static final Set<String> REQUIRED = new HashSet<>();

    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext) throws Throwable {

        Needs annotationOnClass = invocationContext.getExecutable().getDeclaringClass().getAnnotation(Needs.class);
        read(annotationOnClass, extensionContext);
        Needs annotation = invocationContext.getExecutable().getAnnotation(Needs.class);
        read(annotation, extensionContext);


        invocation.proceed();
    }

    @SuppressWarnings("unchecked")
    protected void read(Needs annotation, ExtensionContext extensionContext) {
        if (annotation != null) {
            Set<String> neededObjects = (Set<String>) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent(NEEDED, (k) -> REQUIRED);
            neededObjects.addAll(Arrays.asList(annotation.value()));
            log.info("Needed objects {}", neededObjects);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Needs {
        String[] value();
    }
}
