package nl.vpro.rules;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.MDC;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class TestMDC extends TestWatcher {

    public static final String KEY = "currenttest";

    @Override
    protected void starting(Description d) {
        MDC.put(KEY, d.getTestClass().getSimpleName() + "#" + d.getMethodName());
    }

    @Override
    protected void finished(Description d) {
        MDC.remove(KEY);
    }

}
