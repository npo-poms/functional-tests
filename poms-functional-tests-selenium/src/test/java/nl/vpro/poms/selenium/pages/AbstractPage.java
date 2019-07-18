package nl.vpro.poms.selenium.pages;

import com.paulhammant.ngwebdriver.NgWebDriver;
import lombok.extern.slf4j.Slf4j;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

@Slf4j
public abstract class AbstractPage {

    final WebDriverUtil webDriverUtil;
    final WebDriver driver;
	WebDriverWait wait;
    final Logger log;
    final NgWebDriver ngWait;


    protected AbstractPage(WebDriverUtil util) {
        this.webDriverUtil = util;
        this.driver = util.getDriver();
        this.log = util.getLog();
        this.ngWait = new NgWebDriver((JavascriptExecutor) driver);
        this.wait = new WebDriverWait(driver, 5);
    }
}
