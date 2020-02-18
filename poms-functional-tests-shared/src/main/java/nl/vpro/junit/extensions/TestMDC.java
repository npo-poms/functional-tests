package nl.vpro.junit.extensions;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.extension.*;


/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class TestMDC implements AfterTestExecutionCallback, BeforeTestExecutionCallback {

    public static final String KEY = "currentTest";
    public static final String NUMBER_KEY = "testNumber";


    static final protected AtomicInteger testNumber = new AtomicInteger(0);

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ThreadContext.remove(KEY);
        ThreadContext.remove(NUMBER_KEY);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        ThreadContext.put(KEY, context.getRequiredTestClass().getSimpleName() + "#" + context.getRequiredTestMethod().getName());
        ThreadContext.put(NUMBER_KEY, testNumber.incrementAndGet() + ":");
    }

    public static int getTestNumber() {
        return testNumber.get();
    }

}
