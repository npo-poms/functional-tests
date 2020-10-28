package nl.vpro.poms.selenium.poms.pages;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.poms.selenium.pages.AbstractPage;
import nl.vpro.poms.selenium.util.WebDriverUtil;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Search extends AbstractPage {

    private static final By currentUserBy = By.id("currentUser");
    private static final By logoutBy = By.xpath("//a[text()='log uit']");
    private static final By accountInstellingenBy = By.xpath("//a[contains(text(),'account-instellingen')]");
    private static final By menuBy = By.cssSelector(".header-account-buttons > .header-account-link:first-child > span");
    private static final By queryBy = By.cssSelector("input#query");
    private static final By zoekenBy = By.cssSelector("button#submit");
    private static final By wissenBy = By.xpath("//button[contains(text(),'Wissen')]");
    private static final String foundItemTemplate = "span[title='%s']";
    private static final By closeTabBy = By.cssSelector("span.tab-close");
    private static final String criteriaMenuTemplate = ".poms-uiselect[name=%s]";
    private static final String menuOptionTemplate = "//div[contains(text(),'%s')]";
    private static final By datePersonMenuBy = By.xpath("//span[contains(text(), 'Datum & Persoon')]");
    private static final By uitzenddatumBy = By.cssSelector("input#search-datetype-scheduleEventDate");
    private static final By gewijzigdBy = By.cssSelector("input#search-datetype-modified");
    private static final By sorteerdatumBy = By.cssSelector("input#search-datetype-sortdate");
    private static final By vanBy = By.cssSelector("input[name=fromdate]");
    private static final By totEnMetBy = By.cssSelector("input[name=todate]");
    private static final By zoekMetDatumBy = By.cssSelector("a.search-daterange-submit");
    private static final By tagMenuBy = By.cssSelector("[name=Tags] > span");
    private static final By tabInputBy = By.cssSelector("[name=Tags] input");
    private static final String selectedOptionTemplate = "//*[contains(@class,'dropdown-selected') and contains(text(), '%s')]";
    private static final String dropdownSuggestionTemplate = "//span[@ng-switch-when='searchSuggestion' and contains(translate(text(),'ABCDEFGHIJKLMNOPURSTUWXYZ','abcdefghijklmnopurstuwxyz'),translate('%s','ABCDEFGHIJKLMNOPURSTUWXYZ','abcdefghijklmnopurstuwxyz'))]";
    private static final By columnSelectBy = By.cssSelector("div.column-select-icon");
    private static final String columnCheckboxTemplate = "//span[contains(text(),'%s')]/following-sibling::input[@type='checkbox']";
    private static final By tableRowsBy = By.cssSelector("tr.poms-table-row");
    private static final String columCss = "[class*='column-header'][title='%s']";

    public Search(WebDriverUtil util) {
        super(util);
    }

    public void logout() {
        webDriverUtil.waitAndClick(menuBy);
        webDriverUtil.waitAndClick(logoutBy);
        //webDriverUtil.waitForTextToBePresent(loggedOutBy, "Log in");
    }

    public String getCurrentUser() {
        return driver.findElement(currentUserBy).getText();
    }

    public void goToAccountInstellingen() {
        clickMenu();
        webDriverUtil.waitAndClick(accountInstellingenBy);
    }

    private void clickMenu() {
        webDriverUtil.waitAndClick(menuBy);
    }

    public void enterQuery(String query) {
        webDriverUtil.waitAndSendkeys(queryBy, query);
        clickZoeken();
    }

    public void clickZoeken() {
        webDriverUtil.waitAndClick(zoekenBy);
    }

    public boolean itemFound(String title) {
        return webDriverUtil.isElementPresent(By.cssSelector(String.format(foundItemTemplate, title)));
    }

    public void closeTab() {
        webDriverUtil.waitAndClick(closeTabBy);
    }

    public void selectOptionFromMenu(String menu, String option) {
        clickCriteriaMenu(menu);
        webDriverUtil.waitAndClick(By.xpath(String.format(menuOptionTemplate, option)));
    }

    public void clickCriteriaMenu(String menu) {
        final By guiFilter = By.cssSelector(String.format(criteriaMenuTemplate, menu));
        moveToElement(guiFilter);
        wait.until(ExpectedConditions.elementToBeClickable(guiFilter));
        webDriverUtil.waitAndClick(guiFilter);
    }

    public void enterSorteerdatumDates(String start, String end) {
        enterDates(sorteerdatumBy, start, end);
    }

    public void enterUitzenddatumDates(String start, String end) {
        enterDates(uitzenddatumBy, start, end);
    }

    public void enterGewijzigdDates(String start, String end) {
        enterDates(gewijzigdBy, start, end);
    }

    private void clickDatePersonMenu() {
        webDriverUtil.waitAndClick(datePersonMenuBy);
    }

    private void enterDates(By by, String start, String end) {
        clickDatePersonMenu();
        webDriverUtil.waitAndClick(by);
        webDriverUtil.waitAndSendkeys(vanBy, start);
        webDriverUtil.waitAndSendkeys(totEnMetBy, end);
        zoekMetDatum();
    }

    private void zoekMetDatum() {
        webDriverUtil.waitAndClick(zoekMetDatumBy);
    }

    public void enterTags(String tag) {
        webDriverUtil.waitAndClick(tagMenuBy);
        webDriverUtil.waitAndSendkeys(tabInputBy, tag + Keys.ENTER);
    }

    public void removeSelectedOption(String option) {
        webDriverUtil.waitAndClick(By.xpath(String.format(selectedOptionTemplate, option)));
    }

    public List<WebElement> getSuggestions(String key) {
        webDriverUtil.waitAndClick(queryBy);
//        Sleeper.sleep(1000);
        return driver.findElements(By.xpath(String.format(dropdownSuggestionTemplate, key)));
    }

    public void addOrRemoveColumn(String column) {
        webDriverUtil.waitAndClick(columnSelectBy);
        webDriverUtil.waitAndClick(By.xpath(String.format(columnCheckboxTemplate, column)));
        webDriverUtil.waitAndClick(columnSelectBy);
    }

    public boolean isColumnSelectorChecked(String column) {
        webDriverUtil.waitAndClick(columnSelectBy);
        String columnState = webDriverUtil.getAtrributeFrom(By.xpath(String.format(columnCheckboxTemplate, column)), "checked");
        webDriverUtil.waitAndClick(columnSelectBy);
        return "true".equals(columnState);
    }

    public boolean checkIfColumnNameExists(String columnName) {
        return driver.findElements(By.cssSelector(String.format(columCss, columnName))).size() >= 1;
    }

    public MediaItemPage clickRow(int index) {
        webDriverUtil.waitForVisible(tableRowsBy);
        List<WebElement> tableRows = driver.findElements(tableRowsBy);
        WebElement row = tableRows.get(index);
        Actions actions = new Actions(driver);
        actions.moveToElement(row).doubleClick().perform();
        return new MediaItemPage(webDriverUtil);
    }

    // TODO: move to helper class
    private void moveToElement(By by) {
        WebElement element = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);

        //        Actions actions = new Actions(driver);
        //        actions.moveToElement(element).perform();
        //        ((JavascriptExecutor) driver).executeScript(SCROLL_SCRIPT, element);
        //        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }


    public void clickWissen() {
        webDriverUtil.waitAndClick(wissenBy);
    }


    public void getMultibleRowsAndCheckTextEquals(By by, String waardetext) {
        new NgWebDriver((JavascriptExecutor) driver).waitForAngularRequestsToFinish();
        driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .map((WebElement::getText))
                .forEach(item ->
                        assertThat(item).isEqualTo(waardetext)
                );
    }

    public void clickOnColum(String columname) {
        webDriverUtil.waitAndClick(By.cssSelector(String.format(columCss, columname)));
    }

    public String getMidFromColum(int columNumber) {
        webDriverUtil.waitForVisible(By.cssSelector("tbody tr:nth-of-type(" + columNumber + ") [ng-switch-when='mid'] input"));
        WebElement element = driver.findElement(By.cssSelector("tbody tr:nth-of-type(" + columNumber + ") [ng-switch-when='mid'] input"));
        return element.getAttribute("value");
    }

    public MediaItemPage searchAndOpenClip() {
        Search search = new Search(webDriverUtil);
        search.selectOptionFromMenu("Omroepen", "VPRO");
        search.selectOptionFromMenu("MediaType", "Clip");
        search.clickOnColum("Sorteerdatum");
        MediaItemPage item = search.clickRow(0);
        ngWait.waitForAngularRequestsToFinish();
        return item;
    }

}
