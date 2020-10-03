package nl.vpro.testutils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michiel Meeuwissen
 *  */
public class AbstractTest {

    protected static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);


    protected static Logger LOG = LoggerFactory.getLogger(AbstractTest.class);
    protected Logger log = LoggerFactory.getLogger(getClass());

    public void clearCaches() {

    }

}
