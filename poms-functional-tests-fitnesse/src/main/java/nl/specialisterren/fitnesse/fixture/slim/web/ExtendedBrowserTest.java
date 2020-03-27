package nl.specialisterren.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import nl.hsac.fitnesse.fixture.util.selenium.by.TechnicalSelectorBy;
import org.openqa.selenium.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ExtendedBrowserTest extends BrowserTest {
	public Object store(Object result) {
		return result;
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
     * @param regex a regex that matches the dd-MM-yyyy to be converted as capture group 1
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
}
