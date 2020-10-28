package nl.vpro.poms.selenium.tests.poms.itemizer;

import javax.annotation.Nonnull;

import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.tests.poms.AbstractPomsTest;
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
        super.logout();
    }

    @Test
    // FAILS if headless
    public void itemizerTest() throws InterruptedException {
        webDriverUtil.waitAndClick(By.id("liveknipper"));
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.waitForVisible(By.id("iconPlayPause"));


        webDriverUtil.waitAndClick(By.id("modal-itemizer-mark-start"));

        wait.until(webDriver -> {
                WebElement webElement = driver.findElement(By.id("iconPlayPause"));
            log.info("{}", webElement.getAttribute("class"));
                return webElement.getAttribute("class").contains("icon-pause");
            }
        );
        Thread.sleep(5000);
        webDriverUtil.waitAndClick(By.id("modal-itemizer-mark-stop"));



        //webDriverUtil.waitAndClick(By.id("liveknipper"));

    }

}
