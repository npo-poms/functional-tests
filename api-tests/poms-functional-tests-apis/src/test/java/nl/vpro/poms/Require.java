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
 * Some tests require some mids to exists. These tests can be annotated with {@link Needs}.
 *
 * This is currently used in {@link AbstractApiMediaBackendTest} to detect that the said mids are needs, and it checks whether they
 * exist, and if not, tries to create them first.
 **
 * @author Michiel Meeuwissen
 */
@Log4j2
public class Require  implements InvocationInterceptor {

    public static final String NEEDED = "NEEDED";

    public static final Set<String> REQUIRED = new HashSet<>();

    private static final Set<String> checked = new HashSet<>();

    public static boolean needsCheck(String id) {
        if (REQUIRED.contains(id) && ! checked.contains(id)) {
            log.info("Checking {}", id);
            checked.add(id);
            return true;
        }
        return false;
    }


    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext) throws Throwable {

        Needs annotationOnClass = invocationContext.getExecutable().getDeclaringClass().getAnnotation(Needs.class);
        read(annotationOnClass, extensionContext);
        Needs annotation = invocationContext.getExecutable().getAnnotation(Needs.class);
        read(annotation, extensionContext);

        if (! REQUIRED.isEmpty() && checked.size() < REQUIRED.size()) {
            log.info("Needed objects {}", REQUIRED);
        }
        invocation.proceed();
    }

    @SuppressWarnings("unchecked")
    protected void read(Needs annotation, ExtensionContext extensionContext) {
        if (annotation != null) {
            Set<String> neededObjects = (Set<String>) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent(NEEDED, (k) -> REQUIRED);
            neededObjects.addAll(Arrays.asList(annotation.value()));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Needs {
        String[] value();
    }
}
