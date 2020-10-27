package nl.vpro.testutils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.extension.*;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class ChangesNotifier implements InvocationInterceptor {

    private static final ThreadLocal<Boolean> NOTIFY = ThreadLocal.withInitial(() -> false);

    static final Object WAIT_NOTIFIABLE = new Object();

    public static void notifyChanges() {
        if (NOTIFY.get()) {
            synchronized (WAIT_NOTIFIABLE) {
                WAIT_NOTIFIABLE.notifyAll();
            }
        }
    }


    public static <T> T interestedInChanges(Callable<T> runnable) throws Exception {
        return interestedInChanges(true, runnable);
    }

    public static <T> T interestedInChanges(boolean interested, Callable<T> runnable) throws Exception {
        NOTIFY.set(interested);
        try {
            return runnable.call();
        } finally {
            NOTIFY.remove();
        }

    }

    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext) throws Throwable {
        boolean interested = extensionContext.getTestMethod().get().getAnnotation(Notify.class) != null;
        interestedInChanges(interested, () -> {
            try {
                return invocation.proceed();
            } catch (Exception e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

        });
     }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Notify {

    }
}
