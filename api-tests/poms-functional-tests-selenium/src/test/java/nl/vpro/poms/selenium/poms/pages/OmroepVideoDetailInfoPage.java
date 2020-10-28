package nl.vpro.poms.selenium.poms.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OmroepVideoDetailInfoPage {

    public final String URN_PREFIX = "urn:vpro:media:";

    private final WebDriver driver;

    public OmroepVideoDetailInfoPage(WebDriver driver){
        this.driver = driver;
    }

    private final By urnClickItemLocation = By.xpath("//span[normalize-space(text())='Urn'][1]");
    private final By urnValueLocation = By.xpath("(//span[normalize-space(text())='Urn'][1]/following::input)[1]");
    private final By subtitelEdit = By.xpath("(//*[normalize-space(.)='Afleveringtitel / Subtitel'])[1]/following::div[@class='edit-icon'][1]");


    private final By subtitelField = By.xpath("(//*[normalize-space(.)='Afleveringtitel / Subtitel'])[1]/following::input[1]");

    private final By subtitelSubmit = By.xpath("(//*[normalize-space(.)='Afleveringtitel / Subtitel'])[1]/following::button[@type='submit'][1]");


    /**
     * Checks if the urn value extracted contains the fixed prefix
     *
     * @return true: urn with prefix present
     * false: urn does not have the prefix
     */
    public boolean checkUrnPresent(){

        //select to show urn
        driver.findElement(urnClickItemLocation).click();

        try {
            Thread.sleep(1000);
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }

        //retrieve urn value
        String datavalue = driver.findElement(urnValueLocation).getAttribute("value");

        System.out.println("opgehaalde urn: " + datavalue);
        //does it start with the URN PREFIX?
        if(datavalue.startsWith(URN_PREFIX)){

            return true;

        }
        else {
            return false;
        }
    }

    public void fillInStandardField(String textToEnter, By xpathEdit, By xpathField, By xpathSubmit){
        System.out.println("fill in standard field");
        driver.findElement(xpathEdit).click();

        driver.findElement(xpathField).sendKeys(textToEnter);

        driver.findElement(xpathSubmit).click();

        try {
            Thread.sleep(15000);
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }
    }

    public void enterDataOtherFields(String uniqueDetector){
        System.out.println("Enter data other fields");
        fillInStandardField(uniqueDetector + "subtitel", subtitelEdit, subtitelField, subtitelSubmit);
    }
}
