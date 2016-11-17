package nl.vpro.poms;


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Michiel Meeuwissen
 */
public class DoAfterException implements TestRule {

    final Runnable job;

    public DoAfterException(Runnable job) {
        this.job = job;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    job.run();
                    throw t;
                }
            }
        };

    }
}
