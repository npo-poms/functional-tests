package nl.vpro.poms;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.MDC;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class TestMDC extends TestWatcher {

    @Override
    protected void starting(Description d) {
        MDC.put("currenttest", d.getTestClass().getSimpleName() + "#" + d.getMethodName());
    }

    @Override
    protected void finished(Description d) {
        MDC.remove("currenttest");
    }

}
