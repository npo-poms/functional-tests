package nl.vpro.poms.selenium.util.expectedconditions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * @author Michiel Meeuwissen
 */
public class PageLoaded implements ExpectedCondition<Boolean> {
    String expectedTitle;

    public PageLoaded(String expectedTitle) {
        this.expectedTitle = expectedTitle;
    }

    @Override
        public Boolean apply(WebDriver driver) {
        return driver.getTitle().equals(expectedTitle);
    }

}
