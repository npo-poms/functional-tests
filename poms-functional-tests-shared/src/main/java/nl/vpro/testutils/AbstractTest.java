package nl.vpro.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class AbstractTest {

    protected static Logger LOG = LoggerFactory.getLogger(AbstractTest.class);
    protected Logger log = LoggerFactory.getLogger(getClass());

    public void clearCaches() {

    }

}
