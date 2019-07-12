package nl.vpro.poms.selenium.pages;

import com.paulhammant.ngwebdriver.NgWebDriver;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import nl.vpro.poms.selenium.util.WebDriverUtil;

@Slf4j
public abstract class AbstractPage {

    final WebDriver driver;
    final WebDriverUtil waitUtil;
	WebDriverWait wait;
	final NgWebDriver ngWait;


    protected AbstractPage(WebDriver driver) {
        this.waitUtil = new WebDriverUtil(driver);
        this.driver = driver;
        this.ngWait = new NgWebDriver((JavascriptExecutor) driver);
    }

}
