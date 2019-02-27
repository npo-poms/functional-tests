package nl.vpro.poms.selenium.poms.maken;

import org.junit.*;
import org.openqa.selenium.WebDriver;

import nl.vpro.poms.selenium.pages.AccountSettingsOverlayPage;
import nl.vpro.poms.selenium.pages.AddNewObjectOverlayPage;
import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;

import static nl.vpro.poms.selenium.util.Config.CONFIG;


public class CreateUserTest {

    private WebDriver driver;

    @Before
    public void setUp() {
        driver = WebDriverFactory.getWebDriver(Browser.FIREFOX);
    }

    @After
    public void tearDown() {
//        driver.quit();
    }

    @Test
    public void createUser1() {
        loginSpeciaalVf();
        
        Search search = new Search(driver);
        search.clickNew();
        AddNewObjectOverlayPage overlay = new AddNewObjectOverlayPage(driver);
        overlay.chooseMediaType("Clip");
        
        Assert.assertTrue("Button 'Maak Aan' must be disabled", overlay.isDisabledMaakAan());
        overlay.close();
        logout();
    }
    
    @Test
    public void createUser2() {
    	loginSpeciaalVf();
    	
    	Search search = new Search(driver);
        search.clickNew();
        AddNewObjectOverlayPage overlay = new AddNewObjectOverlayPage(driver);
        overlay.enterTitle("Clip" + DateFactory.getNow());
        overlay.chooseMediaType("Clip");
        overlay.chooseAvType("Video");
        overlay.chooseGenre("Jeugd");
        Assert.assertFalse("Button 'Maak Aan' must be enabled", overlay.isDisabledMaakAan());
        overlay.clickMaakAan();
        
    	logout();
    }
    
    @Test
    public void createUser3() {
    	loginSpeciaalVf();
    	
    	Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	overlayPage.addStandaardOmroep("NPS");
    	overlayPage.clickOpslaan();
    	search.clickNew();
    	
    	AddNewObjectOverlayPage overlay = new AddNewObjectOverlayPage(driver);
    	Assert.assertTrue("Standard omroep NPS is present", overlay.omroepIsSelected("NPS"));
    	overlay.close();
    	
    	logout();
    }
    
	private void loginSpeciaalVf() {
		Login login = new Login(driver);
        login.gotoPage();
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        login.login(user, password);
	}
	
	private void logout() {
		Search search = new Search(driver);
		search.logout();
	}
}
