package nl.vpro.poms;


import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Michiel Meeuwissen
 */
public class AllowUnavailable implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (javax.ws.rs.ServiceUnavailableException | java.net.ConnectException se) {
                    throw new AssumptionViolatedException(description.toString() + ":" + se.getMessage() + " " + se.getMessage());
                }
            }
        };

    }
}
