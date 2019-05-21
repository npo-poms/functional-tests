package nl.vpro.poms.selenium.pages;

import net.bytebuddy.asm.Advice;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class Search extends AbstractPage {

    private static final By currentUserBy = By.id("currentUser");
    private static final By newBy = By.cssSelector(".header-link-new");
    private static final By logoutBy = By.xpath("//a[text()='log uit']");
    private static final By accountInstellingenBy = By.xpath("//a[contains(text(),'account-instellingen')]");
    private static final By loggedOutBy = By.cssSelector("div#msg > h2");
    private static final By menuBy =
            By.cssSelector(".header-account-buttons > .header-account-link:first-child > span");
    private static final By overlayFormBy = By.cssSelector("div.modal-backdrop");

    private static final By queryBy = By.cssSelector("input#query");
    private static final By zoekenBy = By.cssSelector("button#submit");
    private static final By wissenBy = By.xpath("//button[contains(text(),'Wissen')]");
    private static final By resultTableBy = By.cssSelector("table.search-results-list");
    private static final String foundItemTemplate = "span[title='%s']";
    private static final By closeTabBy = By.cssSelector("span.tab-close");
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


    private static final String SCROLL_SCRIPT =
            "window.scrollBy(0,(-window.innerHeight + arguments[0].getBoundingClientRect().top + arguments[0].getBoundingClientRect().bottom) / 2);";

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

    public void closeTab() {
        waitUtil.waitAndClick(closeTabBy);
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

    public void getSearchRowSorteerDatumKanaal(){
        waitUtil.waitForVisible(By.cssSelector("tr td [ng-if*='sortDateScheduleEvent']"));
    }

    public String getSearchRowSorteerDatumKanaal(String sorteerdatum){
        waitUtil.waitForVisible(By.xpath("(//tr/descendant::*[contains(text(),'"+sorteerdatum+"')]/descendant::*)[1]"));
        return driver.findElement(By.xpath("(//tr/descendant::*[contains(text(),'"+sorteerdatum+"')]/descendant::*)[1]")).toString();
    }


}
