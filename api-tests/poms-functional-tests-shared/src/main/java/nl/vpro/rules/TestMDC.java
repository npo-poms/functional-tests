package nl.vpro.rules;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.MDC;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class TestMDC extends TestWatcher {

    public static final String KEY = "currentTest";
    public static final String NUMBER_KEY = "testNumber";


    static final protected AtomicInteger testNumber = new AtomicInteger(0);
    @Override
    protected void starting(Description d) {


        MDC.put(KEY, d.getTestClass().getSimpleName() + "#" + d.getMethodName());
        MDC.put(NUMBER_KEY, String.valueOf(testNumber.incrementAndGet()) + ":");
    }

    @Override
    protected void finished(Description d) {
        MDC.remove(KEY);
        MDC.remove(NUMBER_KEY);
    }

    public int getTestNumber() {
        return testNumber.get();
    }

}
