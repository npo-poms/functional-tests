package nl.vpro.poms.selenium.npoapi.thesaurus;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import nl.vpro.api.client.utils.Config;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.rules.DoAfterException;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 *
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class ThesaurusPopupTest extends AbstractTest {

    private static final String EXAMPLE_TITLE = "POMS GTAA";
    private static final String POPUP_TITLE = "GTAA";
    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            ThesaurusPopupTest.exception = t;
        }
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }



    public ThesaurusPopupTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Test
    public void test000login() {
        String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/";
        login(url).gtaaBrowserTest();
    }


    /**
     * Logs in to the thesaurus frontend.
     */
    @Test
    public void test001Login() {
        webDriverUtil.waitForTitle(EXAMPLE_TITLE);

    }

    /**
     * Searches for a person with the Given name: Jan Peter and Family name Balkenende.
     * A list should appear with two values containing "Jan Peter"

     */
    @Test
    public void test002FindJanPeter() throws InterruptedException {
        driver.findElement(By.id("value")).sendKeys("Jan Peter Balkenende");
        driver.findElement(By.id("open")).click();

        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);
        waitUntilSuggestionReady();

        long counter = driver.findElements(By.xpath("//ul/li"))
                .stream()
                .filter(s -> s.getText().contains("Jan Peter"))
                .count();

        assertThat(counter).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void test003SelectOne() throws  IOException {

        WebElement jan_peter = driver.findElements(By.xpath("//ul/li"))
            .stream()
            .filter(s -> s.getText().contains("Jan Peter"))
            .findFirst()
            .get();
        jan_peter.click();
        webDriverUtil.waitForAngularRequestsToFinish();
        // There should no appear a 'select'

        WebElement select = driver.findElement(By.id("submit"));
        // and click it

        select.click();

        // Now, the window should disspear
        try {
            String url = driver.getCurrentUrl();
            fail("getCurrentUrl should give exception since the window is closed. But it resulted " + url);
        } catch(Exception e) {
            log.info(e.getMessage());
        }

        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);

        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement jsonArea = driver.findElement(By.id("json"));
        String json = jsonArea.getAttribute("value");

        JsonNode jsonNode = Jackson2Mapper.getLenientInstance().readTree(new StringReader(json));
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");

    }

    @Test
    public void test004Geonames() throws InterruptedException {

        WebElement schemes = driver.findElement(By.id("schemes"));
        Select select = new Select(schemes);
        select.deselectAll();
        select.selectByValue("geographicname");
        WebElement value = driver.findElement(By.id("value"));
        value.clear();
        value.sendKeys("Amsterdam");
        driver.findElement(By.id("open")).click();
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);

        waitUntilSuggestionReady();

        List<WebElement> elements = driver.findElements(By.xpath("//ul/li"));
        Iterator<WebElement> i = elements.iterator();
        WebElement register = null;
        while(i.hasNext()) {
            WebElement e = i.next();
            String[] lines = e.getText().split("\n");
            log.info("{}", Arrays.asList(lines));
            if (i.hasNext()) {
                assertThat(lines[0]).endsWith("[geografische naam]");
            } else {
                register = e;

            }
        }

        assertThat(elements).hasSizeGreaterThan(3);
        assertThat(register).isNotNull();



    }

    private void waitUntilSuggestionReady() {
        webDriverUtil.getWait().until(webDriver ->
                ! webDriver.findElement(By.id("searchValue")).getAttribute("class").contains("waiting"));
    }


}
