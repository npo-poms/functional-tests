package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class Cas {
    static final By loggedOutBy = By.cssSelector("div#msg > h2");
    static final By successFullLogout = By.cssSelector("div#msg.success");
}
