package nl.specialisterren.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import nl.hsac.fitnesse.fixture.util.selenium.by.TechnicalSelectorBy;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;

public class ExtendedBrowserTest extends BrowserTest {
    private LinkedList<String> previousPropertyValues = new LinkedList<>();

    public Object store(Object result) {
        return result;
    }
	
    public String trySetBrowserSizeToBy(int newWidth, int newHeight) {
        try {
            setBrowserSizeToBy(newWidth, newHeight);
            return "";
        } catch(SlimFixtureException e) {
            return e.getMessage();
        }
    }
	
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean waitForNotVisible(String place) {
        return !waitForVisibleIn(place, null);
    }
	
    public boolean isPresentOnPage(String place) {
        return isVisibleOnPage(place);
    }
	
    public void removeLastSearchContext() {
        List<String> currentSearchContextPath = getCurrentSearchContextPath();
		int size = currentSearchContextPath.size();
        
        if (size == 1)
            clearSearchContext();
        else if (size > 1) {
            currentSearchContextPath.remove(size-1);          
            setSearchContextTo(currentSearchContextPath.get(size-2));
        }
    }

    public void zetCheckboxOpWaarde(String place, String huidig, String nieuw){

        if(huidig.equals(nieuw))
            return ;

        getSeleniumHelper().getElement(place).click();

    }

    public void dragAndDropFromTo(String from, String to){

        WebElement element = getSeleniumHelper().findElement(By.xpath(from));

        WebElement target = getSeleniumHelper().findElement(By.xpath(to));

        Actions builder = new Actions(getSeleniumHelper().driver());

        builder.clickAndHold(element)
                .moveToElement(target)
                .release(target)
                .build().perform();
    }

    @WaitUntil
    public void checkPendingRequests() {
        int maxQuite = 0;
        for(int breakOut=0;breakOut<1200;breakOut++) {
            waitMilliseconds(5);
            final Object numberOfAjaxConnections = getSeleniumHelper().executeJavascript("return window.openHTTPs");
            // return should be a number
            if (numberOfAjaxConnections instanceof Long) {
                final Long n = (Long) numberOfAjaxConnections;
                if (n.longValue() <= 0L) {
                    if(maxQuite++ > 100) break;
                } else maxQuite = 0;
            } else {
                // If it's not a number, the page might have been freshly loaded indicating the monkey
                // patch is replaced or we haven't yet done the patch.
                monkeyPatchXMLHttpRequest();
            }
        }
    }

    protected void monkeyPatchXMLHttpRequest() {
        try {
            final Object numberOfAjaxConnections = getSeleniumHelper().executeJavascript("return window.openHTTPs");
            if (numberOfAjaxConnections instanceof Long) {
                return;
            }
/*            final String script = "  (function() {" + "var oldOpen = XMLHttpRequest.prototype.open;" + "window.openHTTPs = 0;" +
                    "XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {" + "window.openHTTPs++;" +
                    "this.addEventListener('readystatechange', function() {" + "if(this.readyState == 4) {" + "window.openHTTPs--;" + "}" +
                    "}, false);" + "oldOpen.call(this, method, url, async, user, pass);" + "}" + "})();";*/
            final String script =
                    "(function() {" +
                            "var oldOpen = XMLHttpRequest.prototype.open;" +
                            "window.openHTTPs = 0;" +
                            "XMLHttpRequest.prototype.specialisterrenEventListenerAdded = false;" +
                            "XMLHttpRequest.prototype.specialisterrenCountsAsOpen = false;" +
                            "XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {" +
                            "if(!this.specialisterrenEventListenerAdded) {" +
                            "this.addEventListener('readystatechange', function() {" +
                            "if(this.readyState > 0 && this.readyState < 4) {" +
                            "if(!this.specialisterrenCountsAsOpen) {" +
                            "window.openHTTPs++;" + "this.specialisterrenCountsAsOpen = true;" +
                            "}" +
                            "} else {" +
                            "if(this.specialisterrenCountsAsOpen) {" +
                            "window.openHTTPs--;" + "this.specialisterrenCountsAsOpen = false;" +
                            "}" +
                            "}" +
                            "}, false);" +
                            "this.specialisterrenEventListenerAdded = true;" +
                            "}" +
                            "oldOpen.call(this, method, url, async, user, pass);" +
                            "}" +
                            "})();";
            getSeleniumHelper().executeJavascript(script);
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Checks if the dates in all visible (on page) elements matching place compare as specified within the timeout.
     * Warning: this method may scroll elements into view, make sure that any subsequent actions do not depend on
     * scroll position!
     * @param place a technical selector (i.e. starting with id=, css=, xpath=, name=, link=, partialLink=).
     * @param regex a regex that matches the dd-MM-yyyy to be converted as capture group 1.
     * @param comparison the way the matching numbers should be compared (possibilities: < <= == >= >).
     * @param compareWith the dd-MM-yyyy to compare the matching numbers with.
     * @return true if the dates in the matching elements compare as specified within the timeout, false otherwise.
     * @throws DateTimeParseException if the text of one of the matching elements cannot be converted to LocalDate.
     */
    @WaitUntil
    public boolean datesInElementsMatchedWithCompareWith(String place, String regex, String comparison, String compareWith) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", new Locale("nl", "NL"));
        LocalDate compareWithDate;
        try {
            compareWithDate = LocalDate.parse(compareWith, formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Unable to convert compareWith parameter (\"" + compareWith + "\") to LocalDate!", e);
        }
        List<WebElement> matchingElements = getSeleniumHelper().findElements(TechnicalSelectorBy.forPlace(place));
        List<WebElement> visibleMatchingElements = displayedElementsOf(matchingElements);
        if (visibleMatchingElements.size() == 0) {
            return false;
        }
        Pattern matchedWith;
        try {
            matchedWith = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new RuntimeException("Unable to compile regex parameter (\"" + regex + "\") to Pattern!", e);
        }
        for (WebElement e : visibleMatchingElements) {
            String elementText = getSeleniumHelper().getText(e);
            if (elementText.isEmpty()) {
                // Elements that are not in the viewport sometimes incorrectly return an empty string
                // As a workaround, scroll them into view to get their true contents
                scrollTo(e);
                return false;
            }
            Matcher m = matchedWith.matcher(elementText);
            if (!m.find()) {
                throw new RuntimeException("No match for pattern (\"" + regex + "\") in element contents (\"" + elementText + "\")!");
            } else {
                LocalDate currentDate = LocalDate.parse(m.group(1), formatter);
                boolean comparisonOutcome;
                switch (comparison) {
                    case "<":
                        comparisonOutcome = currentDate.isBefore(compareWithDate);
                        break;
                    case "<=":
                        comparisonOutcome = currentDate.isBefore(compareWithDate) || currentDate.equals(compareWithDate);
                        break;
                    case "==":
                        comparisonOutcome = currentDate.equals(compareWithDate);
                        break;
                    case ">=":
                        comparisonOutcome = currentDate.isAfter(compareWithDate) || currentDate.equals(compareWithDate);
                        break;
                    case ">":
                        comparisonOutcome = currentDate.isAfter(compareWithDate);
                        break;
                    default:
                        throw new RuntimeException("Invalid comparison argument passed to numbersInElementsCompareWith;" +
                                " please specify one of < <= == >= > !");
                }
                if (!comparisonOutcome) {
                    return false;
                }
            }
        }
        return true;
    }

    protected List<WebElement> displayedElementsOf(List<WebElement> elements) {
        List<WebElement> visibleElements = new ArrayList<>();
        for (WebElement e : elements) {
            if (e.isDisplayed()) {
                visibleElements.add(e);
            }
        }
        return visibleElements;
    }

    /**
     * Waits until the specified property of the specified element becomes stable.
     * This method checks if the property has the same value three times in a row while polling with the WaitUntil
     * interval.
     * @param property the name of the property to check for stability, e.g. <code>scrollTop</code>.
     * @param place a technical selector (i.e. starting with id=, css=, xpath=, name=, link=, partialLink=).
     * @return true if the property has the same value three times in a row, false otherwise.
     */
    @WaitUntil
    public boolean waitUntilPropertyOfElementIsStable(String property, String place) {
        WebElement e = getSeleniumHelper().findElement(TechnicalSelectorBy.forPlace(place));
        if (e == null) {
            // If the element is not there (yet), just return false so we keep waiting
            return false;
        }

        String currentValue = getSeleniumHelper().executeJavascript("return arguments[0][arguments[1]]", e, property).toString();

        if (previousPropertyValues.size() == 2
                && previousPropertyValues.getFirst().equals(previousPropertyValues.getLast())
                && currentValue.equals(previousPropertyValues.getLast())) {
            System.out.println("Property \"" + property + "\" of element " + e + " became stable at value: " + currentValue);
            previousPropertyValues.clear();
            return true;
        } else {
            previousPropertyValues.addLast(currentValue);
            if (previousPropertyValues.size() > 2) {
                previousPropertyValues.removeFirst();
            }
            System.out.println("Waiting for property \"" + property + "\" of element " + e + " to become stable. Current value: " + currentValue);
            return false;
        }
    }

    @Override
    public boolean switchToNextTab() {
        List<String> tabs = getTabHandles();
        if (tabs.size() == 1 && getCurrentTabIndex(tabs) < 0) {
            // There is only one open tab, but it is not the current one. Switch to the only open tab
            goToTab(tabs, 0);
            return true;
        } else {
            return super.switchToNextTab();
        }
    }

    @Override
    public boolean switchToPreviousTab() {
        List<String> tabs = getTabHandles();
        if (tabs.size() == 1 && getCurrentTabIndex(tabs) < 0) {
            // There is only one open tab, but it is not the current one. Switch to the only open tab
            goToTab(tabs, 0);
            return true;
        } else {
            return super.switchToPreviousTab();
        }
    }
	
    public String getSortDate(WebElement element) {
        ArrayList<String> values = null;
        if (element != null) {
            WebElement item = element.findElement(By.cssSelector(".column-sortDate"));
            if (item != null)
                return getElementText(item);
        }
        return null;
    }
	
    public ArrayList<String> sortDatesOfIn(String place, String container) {
        ArrayList<String> values = null;
        WebElement element = getElementToRetrieveValue(place, container);
        if (element != null) {
            element = element.findElement(By.tagName("tbody"));
            if (element != null) {
                values = new ArrayList<String>();
                String tagName = element.getTagName();
                if ("tbody".equalsIgnoreCase(tagName)) {
                    List<WebElement> items = element.findElements(By.tagName("tr"));
                    for (WebElement item : items) {
                        if (item.isDisplayed()) {
                            values.add(getSortDate(item));
                        }
                    }
                }
            }
        }
        return values;
    }
	
    public String listAllSortDatesOfIn(String place, String container) {
        String result = null;
        List<String> values = sortDatesOfIn(place, container);
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<div><ul>");
            for (String value : values) {
                sb.append("<li>");
                sb.append(value);
                sb.append("</li>");
            }
            sb.append("</ul></div>");
            result = sb.toString();
        }
        return result;
    }
	
    public String listAllSortDatesOf(String place) {
        return listAllSortDatesOfIn(place, null);
    }
	
    public ArrayList<String> sortDatesOf(String list) {
        ArrayList<String> values = new ArrayList<String>();
        Pattern pattern = Pattern.compile("<li>.+?</li>");
        Matcher matcher = pattern.matcher(list);
        while (matcher.find())
        {
            String item = matcher.group();
            Pattern p = Pattern.compile("<li>(\\d{2}-\\d{2}-\\d{4}) .+?(\\n.+?)*?</li>");
            Matcher m = p.matcher(item);
            if (m.find())
                values.add(m.group(1));
        }
        return values;
    }
	
    public ArrayList<Boolean> datesAreDate(String list, String comparison, String date2) throws ParseException {
        ArrayList<Boolean> result = new ArrayList<Boolean>();
        ArrayList<String> dates = sortDatesOf(list);
        Date dateToCompare2 = new SimpleDateFormat("dd-MM-yyyy").parse(date2); 
        	
        for (String date : dates) {
            Date dateToCompare1 = new SimpleDateFormat("dd-MM-yyyy").parse(date); 
            
            switch (comparison) {
                case ">=":
                    result.add(dateToCompare1.after(dateToCompare2) || dateToCompare1.equals(dateToCompare2));
                    break;
                
                case "==":
                    result.add(dateToCompare1.equals(dateToCompare2));
                    break;
                	
                default:
                    result.add(false);
            }
        }
        
        return result;
	}
	
    public Boolean listAllTrue(ArrayList<Boolean> list) {
        for (Boolean item : list) {
            if (!item)
                return false;
        }
        
        return true;
    }
	
    public ArrayList<String> valuesOfMatching(ArrayList<String> values, String regex) {
        ArrayList<String> result = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        	
        for(String value : values) {
            Matcher m = p.matcher(value);
            if (m.find()) {
                result.add(m.group(1));
            }
        }
        	
		return result;
    }
	
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean waitForVisibleAt(String place, ArrayList<String> values) {
        return waitForVisibleAtIn(place, values, null);
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean waitForNotVisibleAt(String place, ArrayList<String> values) {
        return !waitForVisibleAtIn(place, values, null);
    }
	
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean waitForVisibleAtIn(String place, ArrayList<String> values, String container) {
        Boolean result = Boolean.TRUE;
        for (String value : values) {
            result = result && waitForVisibleIn(place + "[starts-with(normalize-space(), '" + value + "')]", container);
        }
        return result;
    }
	
    public int getNumberOfTabs() {
        WebElement element = getElement("xpath=//ul[@class='nav nav-tabs']");
		
        // items consist of all tabs (li.tab) and li.tab-add if number of tabs is 1
        // items consist of all tabs (li.tab), li.tab-add and li.tab-close-all if number of tabs is at least 2
        List<WebElement> items = element.findElements(By.tagName("li"));
		
        int liCount = items.size();
        if (liCount > 2) {
            liCount--; // exclude li.tab-close-all
        }
        liCount--; // exclude li.tab-add
		
        return liCount;
    }
	
    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean waitUntilNumberOfTabsIs(int numberOfTabs) {
        return (getNumberOfTabs() == numberOfTabs);
    }
	
    public void clickLastTab() {
        if (getNumberOfTabs() > 1) {
            click("xpath=//ul[@class='nav nav-tabs']//li[last()-2]");
        } else {
            click("xpath=//ul[@class='nav nav-tabs']//li[last()-1]");
        }
    }
	
    public void closeLastTab() {
        click("xpath=//ul[@class='nav nav-tabs']/li[last()-2]/a/tab-heading/span/span[text()='close']");
    }
	
    public void clearOnlyTab() {
        click("xpath=//ul[@class='nav nav-tabs']/li[last()-1]/a/tab-heading/span/span[text()='close']");
    }
	
    public void closeAllTabs() {
        // li.tab-close-all doesn't close or clear the first tab
        click("css=li.tab-close-all");
        
        clearOnlyTab();
    }
}
