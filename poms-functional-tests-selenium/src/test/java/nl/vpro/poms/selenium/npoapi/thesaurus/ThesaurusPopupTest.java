package nl.vpro.poms.selenium.npoapi.thesaurus;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.gtaa.Scheme;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    @Before
    public void setup() {
        assumeNoException(exceptions.get(ThesaurusPopupTest.class));
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
    public void test002FindJanPeter() {
        selectScheme(Scheme.person);

        driver.findElement(By.id("givenName")).sendKeys("Jan Peter");
        driver.findElement(By.id("familyName")).sendKeys("Balkenende");
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

        // Now, the window should disappear
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson();
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");

    }

    @Test
    public void test004Geonames() throws IOException {
        selectScheme(Scheme.geographicname);
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
        String conceptName = testMethod.getMethodName().replaceAll("[\\[\\]]", "_") + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd'T'HHmmss"));

        List<WebElement> e  =  search(conceptName);
        register = e.get(e.size() -1);
        register.click();

        webDriverUtil.getDriver().findElement(By.id("note")).sendKeys("Made by Selenium test. Don't approve this");
        webDriverUtil.getDriver().findElement(By.id("register")).click();
        webDriverUtil.getWait().until(webDriver -> {
                try {
                    webDriver.findElement(By.id("spinner")).findElement(By.tagName("img"));
                    return true;
                } catch (NoSuchElementException nsee) {
                    return false;
                }
        });
        webDriverUtil.getDriver().findElement(By.id("submit")).click();
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson();
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("status").asText()).isEqualTo("candidate");
        assertThat(jsonNode.get("concept").get("name").asText()).isEqualTo(conceptName);
        assertThat(jsonNode.get("concept").get("scopeNotes").get(0).asText()).isNotEmpty();
    }

    private List<WebElement> search(String value){
        webDriverUtil.getDriver().findElement(By.id("searchValue")).sendKeys(value);
        waitUntilSuggestionReady();
        return driver.findElements(By.xpath("//ul/li"));
    }
    private void waitUntilSuggestionReady() {
        webDriverUtil.getWait().until(webDriver ->
                ! webDriver.findElement(By.id("searchValue")).getAttribute("class").contains("waiting"));
    }
    private void selectScheme(Scheme... scheme) {
        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement schemes = driver.findElement(By.id("schemes"));
        Select select = new Select(schemes);
        select.deselectAll();
        for (Scheme s : scheme) {
            select.selectByValue(s.name());
        }
    }
    private JsonNode getJson() throws IOException {
        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement jsonArea = driver.findElement(By.id("json"));
        String json = jsonArea.getAttribute("value");

        return Jackson2Mapper.getLenientInstance().readTree(new StringReader(json));
    }


}
