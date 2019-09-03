package nl.vpro.poms.selenium.poms.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OmroepVideoDetailInfoPage {

    public final String URN_PREFIX = "urn:vpro:media:";

    private WebDriver driver;

    public OmroepVideoDetailInfoPage(WebDriver driver){
        this.driver = driver;
    }

    private By urnClickItemLocation = By.xpath("//span[normalize-space(text())='Urn'][1]");
    private By urnValueLocation = By.xpath("(//span[normalize-space(text())='Urn'][1]/following::input)[1]");
    private By subtitelEdit = By.xpath("(//*[normalize-space(.)='Afleveringtitel / Subtitel'])[1]/following::div[@class='edit-icon'][1]");
    private By korteTitelEdit = By.xpath("(//*[normalize-space(.)='Korte titel'])[1]/following::div[@class='edit-icon'][1]");
    private By afkortingEdit = By.xpath("(//*[normalize-space(.)='Afkorting'])[1]/following::div[@class='edit-icon'][1]");
    private By werkTitelEdit = By.xpath("(//*[normalize-space(.)='Werktitel'])[1]/following::div[@class='edit-icon'][1]");
    private By origineleTitelEdit = By.xpath("(//*[normalize-space(.)='Originele titel'])[1]/following::div[@class='edit-icon'][1]");
    private By lexicoTitelEdit = By.xpath("(//*[normalize-space(.)='Lexicografische titel'])[1]/following::div[@class='edit-icon'][1]");
    private By korteBeschrijvingEdit = By.xpath("(//*[normalize-space(.)='Korte beschrijving'])[1]/following::div[@class='edit-icon'][1]");
    private By eenregeligeBeschrijvingEdit = By.xpath("(//*[normalize-space(.)='Eenregelige beschrijving'])[1]/following::div[@class='edit-icon'][1]");
    private By tagsEdit = By.xpath("(//*[normalize-space(.)='Tags'])[1]/following::div[@class='edit-icon'][1]");


    private By subtitelField = By.xpath("(//*[normalize-space(.)='Afleveringtitel / Subtitel'])[1]/following::input[1]");
    private By korteTitelField = By.xpath("(//*[normalize-space(.)='Korte titel'])[1]/following::input[1]");
    private By afkortingField = By.xpath("(//*[normalize-space(.)='Afkorting'])[1]/following::input[1]");
    private By werkTitelField = By.xpath("(//*[normalize-space(.)='Werktitel'])[1]/following::input[1]");
    private By origineleTitelField = By.xpath("(//*[normalize-space(.)='Originele titel'])[1]/following::input[1]");
    private By lexicoTitelField = By.xpath("(//*[normalize-space(.)='Lexicografische titel'])[1]/following::input[1]");
    private By korteBeschrijvingField = By.xpath("(//*[normalize-space(.)='Korte beschrijving'])[1]/following::textarea[1]");
    private By eenregeligeBeschrijvingField = By.xpath("(//*[normalize-space(.)='Korte beschrijving'])[1]/following::textarea[1]");
    private By tagsField = By.xpath("(//*[normalize-space(.)='Tags'])[1]/following::input[1]");

    private By subtitelSubmit = By.xpath("(//*[normalize-space(.)='Afleveringtitel / Subtitel'])[1]/following::button[@type='submit'][1]");
    private By korteTitelSubmit = By.xpath("(//*[normalize-space(.)='Korte titel'])[1]/following::button[@type='submit'][1]");
    private By afkortingSubmit = By.xpath("(//*[normalize-space(.)='Afkorting'])[1]/following::button[@type='submit'][1]");
    private By werkTitelSubmit = By.xpath("(//*[normalize-space(.)='Werktitel'])[1]/following::button[@type='submit'][1]");
    private By origineleTitelSubmit = By.xpath("(//*[normalize-space(.)='Originele titel'])[1]/following::button[@type='submit'][1]");
    private By lexicoTitelSubmit = By.xpath("(//*[normalize-space(.)='Lexicografische titel'])[1]/following::button[@type='submit'][1]");
    private By korteBeschrijvingSubmit = By.xpath("(//*[normalize-space(.)='Korte beschrijving'])[1]/following::button[@type='submit'][1]");
    private By eenregeligeBeschrijvingSubmit = By.xpath("(//*[normalize-space(.)='Korte beschrijving'])[1]/following::button[@type='submit'][1]");
    private By tagsSubmit = By.xpath("(//*[normalize-space(.)='Tags'])[1]/following::button[@type='submit'][1]");


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
