package nl.vpro.rules;


import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Michiel Meeuwissen
 */
@Slf4j
public class DoAfterException implements TestRule {

    final Consumer<Throwable> job;

    public DoAfterException(Consumer<Throwable> job) {
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
                    if (t instanceof AssumptionViolatedException) {
                        log.warn(t.getMessage());
                    } else {
                        job.accept(t);
                    }
                    throw t;
                }
            }
        };

    }
}
