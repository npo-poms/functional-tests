package nl.vpro.poms.selenium.pages;

import com.paulhammant.ngwebdriver.NgWebDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Search extends AbstractPage {

    private static final By currentUserBy = By.id("currentUser");
    private static final By newBy = By.cssSelector(".header-link-new");
    private static final By logoutBy = By.xpath("//a[text()='log uit']");
    private static final By accountInstellingenBy = By.xpath("//a[contains(text(),'account-instellingen')]");
    private static final By loggedOutBy = By.cssSelector("div#msg > h2");
    private static final By menuBy = By.cssSelector(".header-account-buttons > .header-account-link:first-child > span");
    private static final By overlayFormBy = By.cssSelector("div.modal-backdrop");
    private static final By queryBy = By.cssSelector("input#query");
    private static final By zoekenBy = By.cssSelector("button#submit");
    private static final By wissenBy = By.xpath("//button[contains(text(),'Wissen')]");
    private static final By resultTableBy = By.cssSelector("table.search-results-list");
    private static final By lastBroadcastTimeChannel = By.cssSelector("[ng-if*='item.lastScheduleEvent']");
    private static final String foundItemTemplate = "span[title='%s']";
    private static final By closeTabBy = By.cssSelector("span.tab-close");
    private static final String closeTabByName = "//*[contains(@class, 'tab-search') or contains(@class, 'tab-edit')]/descendant::*[contains(text(), '%s')]/../../descendant::*[@class='tab-close']";
    private static final String criteriaMenuTemplate = ".poms-uiselect[name=%s]";
    private static final String menuOptionTemplate = "//div[contains(text(),'%s')]";
    private static final By datePersonMenuBy = By.xpath("//span[contains(text(), 'Datum & Persoon')]");
    private static final By uitzenddatumBy = By.cssSelector("input#search-datetype-scheduleEventDate");
    private static final By gewijzigdBy = By.cssSelector("input#search-datetype-modified");
    private static final By aangemaaktBy = By.cssSelector("input#search-datetype-created");
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
    private static final By imagesBy = By.cssSelector("div.media-images");
    private static final By adminBy = By.xpath("//span[contains(text(), 'admin') and contains(@class, 'btn-text-icon-admin')]");
    private static final String adminItemTemplate = "//a[contains(text(), '%s')]";
    private static final String columCss = "[class*='column-header'][title='%s']";
    private static final String SearchItemRow = "tr[class*='poms-table-row']:nth-of-type(%s) [class='column-title'] [title]";
    private static final String SCROLL_SCRIPT = "window.scrollBy(0,(-window.innerHeight + arguments[0].getBoundingClientRect().top + arguments[0].getBoundingClientRect().bottom) / 2);";

    public Search(WebDriver driver) {
        super(driver);
    }

    public void clickNew() {
        waitUtil.waitAndClick(newBy);
    }

    public void logout() {
        waitUtil.waitAndClick(menuBy);
        waitUtil.waitAndClick(logoutBy);
        waitUtil.waitForTextToBePresent(loggedOutBy, "Logout successful");
    }

    public String getCurrentUser() {
        return driver.findElement(currentUserBy).getText();
    }

    public void goToAccountInstellingen() {
        clickMenu();
        waitUtil.waitAndClick(accountInstellingenBy);
    }

    private void clickMenu() {
        waitUtil.waitAndClick(menuBy);
    }

    public void enterQuery(String query) {
        waitUtil.waitAndSendkeys(queryBy, query);
        clickZoeken();
    }

    public void clickZoeken() {
        waitUtil.waitAndClick(zoekenBy);
    }

    public boolean itemFound(String title) {
        return waitUtil.isElementPresent(By.cssSelector(String.format(foundItemTemplate, title)));
    }

    public String getItemListTitle(int itemNumber) {
        waitUtil.waitForVisible(By.cssSelector(String.format(SearchItemRow, itemNumber)));
        return driver.findElement(By.cssSelector(String.format(SearchItemRow, itemNumber))).getText();
    }

    public void clickOnTabWithTitle(By by, String title) {
        driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(webElement -> webElement.getText().equals(title))
                .findFirst().get().click()
        ;
    }

    public void closeTab() {
        waitUtil.waitAndClick(closeTabBy);
    }

    public void closeTabTitle(String title) {
        waitUtil.waitForVisible(By.xpath(String.format(closeTabByName, title)));
        waitUtil.waitAndClick(By.xpath(String.format(closeTabByName, title)));
    }

    public void selectOptionFromMenu(String menu, String option) {
        clickCriteriaMenu(menu);
        waitUtil.waitAndClick(By.xpath(String.format(menuOptionTemplate, option)));
    }

    public void clickCriteriaMenu(String menu) {
        waitUtil.waitAndClick(By.cssSelector(String.format(criteriaMenuTemplate, menu)));
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

    public void enterAangemaaktDates(String start, String end) {
        enterDates(aangemaaktBy, start, end);
    }

    private void clickDatePersonMenu() {
        waitUtil.waitAndClick(datePersonMenuBy);
    }

    private void enterDates(By by, String start, String end) {
        clickDatePersonMenu();
        waitUtil.waitAndClick(by);
        waitUtil.waitAndSendkeys(vanBy, start);
        waitUtil.waitAndSendkeys(totEnMetBy, end);
        zoekMetDatum();
    }

    private void zoekMetDatum() {
        waitUtil.waitAndClick(zoekMetDatumBy);
    }

    public void enterTags(String tag) {
        waitUtil.waitAndClick(tagMenuBy);
        waitUtil.waitAndSendkeys(tabInputBy, tag + Keys.ENTER);
    }

    public void removeSelectedOption(String option) {
        waitUtil.waitAndClick(By.xpath(String.format(selectedOptionTemplate, option)));
    }

    public List<WebElement> getSuggestions(String key) {
        waitUtil.waitAndClick(queryBy);
//        Sleeper.sleep(1000);
        return driver.findElements(By.xpath(String.format(dropdownSuggestionTemplate, key)));
    }

    public void addOrRemoveColumn(String column) {
        waitUtil.waitAndClick(columnSelectBy);
        waitUtil.waitAndClick(By.xpath(String.format(columnCheckboxTemplate, column)));
        waitUtil.waitAndClick(columnSelectBy);
    }

    public boolean isColumnSelectorChecked(String column) {
        waitUtil.waitAndClick(columnSelectBy);
        String columnState = waitUtil.getAtrributeFrom(By.xpath(String.format(columnCheckboxTemplate, column)), "checked");
        waitUtil.waitAndClick(columnSelectBy);
        return "true".equals(columnState);
    }

    public MediaItemPage clickRow(int index) {
        waitUtil.waitForVisible(tableRowsBy);
        List<WebElement> tableRows = driver.findElements(tableRowsBy);
        WebElement row = tableRows.get(index);
        Actions actions = new Actions(driver);
        actions.moveToElement(row).doubleClick().perform();
        return new MediaItemPage(driver);
    }

    public int countRows() {
        waitUtil.waitForVisible(tableRowsBy);
        List<WebElement> tableRows = driver.findElements(tableRowsBy);
        return tableRows.size();
    }

    // TODO: move to helper class
    private void moveToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(SCROLL_SCRIPT, element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    // TODO: To be included?
    public void scrollToAfbeeldingen() {
        WebElement element = driver.findElement(imagesBy);
        moveToElement(element);
        Actions actions = new Actions(driver);
        actions.moveToElement(element).doubleClick().perform();
    }
    // TODO: To be included?
//	public List<WebElement> getTableRows() {
//		wait.until(ExpectedConditions.visibilityOfElementLocated(tableRowsBy));
//		List<WebElement> tableRows = driver.findElements(tableRowsBy);
//		return tableRows;
//	}

    public void clickWissen() {
        waitUtil.waitAndClick(wissenBy);
    }

    public void clickAdminItem(String item) {
        waitUtil.waitAndClick(adminBy);
        waitUtil.waitAndClick(By.xpath(String.format(adminItemTemplate, item)));
    }

    public String getSearchRowSorteerDatumKanaal() {
        waitUtil.waitForVisible(By.cssSelector("tr td [ng-if*='sortDateScheduleEvent']"));
        return driver.findElement(By.cssSelector("tr td [ng-if*='sortDateScheduleEvent']")).getText();
    }

    public String getSearchRowSorteerDatumKanaal(String sorteerdatum) {
        waitUtil.waitForVisible(By.xpath("(//tr/descendant::*[contains(text(),'" + sorteerdatum + "')]/descendant::*)[1]"));
        return driver.findElement(By.xpath("(//tr/descendant::*[contains(text(),'" + sorteerdatum + "')]/descendant::*)[1]")).getText();
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

    public void getMultibleRowsAndCheckTextContains(By by, String waardetext) {
        driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .forEach(item ->
                        assertThat(item).contains(waardetext)
                );
    }

    public void clickOnColum(String columname) {
        waitUtil.waitAndClick(By.cssSelector(String.format(columCss, columname)));
    }

    public void doubleClickOnColum(String columname) {
        waitUtil.waitForVisible(By.cssSelector(String.format(columCss, columname)));

        Actions action = new Actions(driver);
        WebElement element = driver.findElement(By.cssSelector(String.format(columCss, columname)));

        action.doubleClick(element).perform();
    }

    public void getAndCheckTimeBetweenTwoBroadcastsLessThenMinutes(int minutes) {
        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}\\s\\d{2}:\\d{2}");
        List<WebElement> listElementsBoardCastTime = driver.findElements(lastBroadcastTimeChannel);

        List<String> elementText = new ArrayList<String>();

        for(int i=0; i < listElementsBoardCastTime.size();i++){
            if(listElementsBoardCastTime.get(i).isDisplayed()){
                String getElementText = listElementsBoardCastTime.get(i).getText();
                Matcher matcher = pattern.matcher(getElementText);
                matcher.find();
                String TextAfterMatch = matcher.group(0);
                elementText.add(TextAfterMatch);
            }
        }

        for (int i = 0; i < elementText.size(); i++) {
            if (i + 1 < elementText.size()) {

                ZonedDateTime varFirstDateTime = ZonedDateTime.parse(elementText.get(i), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.of("Europe/Amsterdam")));
                ZonedDateTime varSecondDateTime = ZonedDateTime.parse(elementText.get(i+1), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.of("Europe/Amsterdam")));

                Long durationBetweenMinutes = Duration.between(varFirstDateTime, varSecondDateTime).toMinutes();

                assertThat(durationBetweenMinutes).isNotNegative().isLessThan(minutes);
            }
        }
    }
    public String getMidFromColum(int columNumber){
        return waitUtil.waitAndGetText(By.cssSelector("tbody tr:nth-of-type("+columNumber+") [ng-switch-when='mid']"));
    }
}
