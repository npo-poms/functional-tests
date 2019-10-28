package nl.vpro.rules;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.*;
import org.slf4j.MDC;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class TestMDC implements AfterTestExecutionCallback, BeforeTestExecutionCallback {

    public static final String KEY = "currentTest";
    public static final String NUMBER_KEY = "testNumber";


    static final protected AtomicInteger testNumber = new AtomicInteger(0);

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        MDC.remove(KEY);
        MDC.remove(NUMBER_KEY);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        MDC.put(KEY, context.getRequiredTestClass().getSimpleName() + "#" + context.getRequiredTestMethod().getName());
        MDC.put(NUMBER_KEY, String.valueOf(testNumber.incrementAndGet()) + ":");
    }

    public int getTestNumber() {
        return testNumber.get();
    }

}
