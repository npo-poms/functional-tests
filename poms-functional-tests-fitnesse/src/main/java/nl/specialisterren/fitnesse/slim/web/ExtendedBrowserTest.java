package nl.specialisterren.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import org.openqa.selenium.*;

import java.util.List;

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
}
