package nl.vpro.testutils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Michiel Meeuwissen
 *  */
public class AbstractTest {


    protected static Logger LOG = LogManager.getLogger(AbstractTest.class);
    protected Logger log = LogManager.getLogger(getClass());

    public void clearCaches() {

    }

}
