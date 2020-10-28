package nl.vpro.poms.selenium;

import io.github.bonigarcia.wdm.config.DriverManagerType;

import java.lang.annotation.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.api.client.utils.Config;
import nl.vpro.junit.extensions.TestMDC;
import nl.vpro.poms.selenium.pages.*;
import nl.vpro.poms.selenium.poms.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;
import nl.vpro.test.jupiter.AbortOnException;

/**
 * This is an effort to make a new base class for the selenium tests but based on junit 5.
 *
 * This is undoable: See https://github.com/junit-team/junit5/issues/878)
 *
 * You cannot  in the mean time parameterize a class any more. You can give every single test a browser argument, and create the utilities on the fly, but this is hardly nicer.
 *
 * Since browser testing is typically done for several different browsers, and every test should be done on every browser,the way to go is simply class parameterization. We'll simply await a fix for #878, which will be 'soon'.
 *
 */
@Timeout(value = 5, unit = TimeUnit.MINUTES)
@ExtendWith({TestMDC.class, AbortOnException.class})
public abstract class AbstractTest5 {
    static final Logger LOG = LoggerFactory.getLogger(AbstractTest5.class);
    protected Logger log = LoggerFactory.getLogger(getClass());


    public static final Config CONFIG =
            new Config("npo-functional-tests.properties", "npo-browser-tests.properties");

    protected static Map<Browser, WebDriver> staticDrivers = new HashMap<>();
    protected static Map<Class<?>, Boolean> loggedAboutSetupEach = new HashMap<>();
    protected boolean setupEach = true;


    protected AbstractTest5() {
        this.setupEach = this.getClass().getAnnotation(TestMethodOrder.class) == null;
        if (!this.setupEach && !loggedAboutSetupEach.getOrDefault(getClass(), false)) {
            log.info("\nRunning" + getClass() + " with fixed method order, so keeping the driver between the tests");
            loggedAboutSetupEach.put(getClass(), true);
        }
    }



    @AfterEach
    public void tearDown() {
        if (setupEach) {
          //  if (driver != null) {
                /**
                 * In head mode we experience the issue with driver.close()
                 * org.openqa.selenium.NoSuchSessionException: Tried to run command without establishing a connection
                 */
            //    if (WebDriverFactory.headless) driver.close();
              //  driver.quit();
        //}
        }
    }

    @AfterAll
    public static void tearDownClass(List<Exception> exceptions) {
        if (exceptions.isEmpty() || WebDriverFactory.headless) {
            for (WebDriver wd : staticDrivers.values()) {
                wd.quit();
            }
            staticDrivers.clear();
        } else {
            LOG.warn("Not closing browser because of test failures {}", exceptions);
        }
    }

}
