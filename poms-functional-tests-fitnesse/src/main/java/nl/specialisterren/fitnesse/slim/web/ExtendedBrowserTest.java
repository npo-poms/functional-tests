package nl.specialisterren.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import org.openqa.selenium.*;

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
}
