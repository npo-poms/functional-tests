package nl.vpro.poms.selenium.pages;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.WebDriver;

import nl.vpro.api.client.utils.Config;

@Slf4j
public abstract class AbstractPage {

    WebDriver driver;
    
    protected AbstractPage(WebDriver driver) {
        this.driver = driver;
    }
}
