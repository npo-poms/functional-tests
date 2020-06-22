package nl.vpro.poms.selenium.poms.tests.itemizer;

import javax.annotation.Nonnull;

import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.poms.tests.AbstractPomsTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

public class LiveItemizerTest extends AbstractPomsTest {


    public LiveItemizerTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Before
    public void setup() {
        login().speciaalVf();
    }

    @After
    public void teardown() {
        //super.logout();
    }

    @Test

    public void itemizerTest() throws InterruptedException {
        webDriverUtil.waitAndClick(By.id("liveknipper"));
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.waitForVisible(By.id("iconPlayPause"));
		wait.until(webDriver -> {
                WebElement webElement = driver.findElement(By.id("iconPlayPause"));
                log.info("{}", webElement.getAttribute("class"));
                return webElement.getAttribute("class").contains("icon-pause");
            }
        );
		// let it play for 10 seconds
        Thread.sleep(10000);

        //webDriverUtil.waitAndClick(By.id("liveknipper"));

    }

}
