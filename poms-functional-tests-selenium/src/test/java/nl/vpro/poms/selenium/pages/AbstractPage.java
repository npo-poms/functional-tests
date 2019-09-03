package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.poms.selenium.util.WebDriverUtil;

public abstract class AbstractPage {

    protected final WebDriverUtil webDriverUtil;
    protected final WebDriver driver;
	protected final WebDriverWait wait;
    protected final Logger log;
    protected final NgWebDriver ngWait;


    protected AbstractPage(WebDriverUtil util) {
        this.webDriverUtil = util;
        this.driver = util.getDriver();
        this.log = util.getLog();
        this.ngWait = new NgWebDriver((JavascriptExecutor) driver);
        this.wait = new WebDriverWait(driver, 5);
    }
}
