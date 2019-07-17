package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import nl.vpro.poms.selenium.util.WebDriverUtil;


public abstract class AbstractPage {

    final WebDriverUtil webDriverUtil;
    final WebDriver driver;
    final Logger log;



    protected AbstractPage(WebDriverUtil util) {
        this.webDriverUtil = util;
        this.driver = util.getDriver();
        this.log = util.getLog();
    }

}
