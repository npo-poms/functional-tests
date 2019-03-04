package nl.vpro.poms.selenium.poms.maken;

import org.junit.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.support.ui.Sleeper;

import com.paulhammant.ngwebdriver.NgWebDriver;

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
        driver = WebDriverFactory.getWebDriver(Browser.CHROME);
        NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
    }

    @After
    public void tearDown() {
//        driver.quit();
    }

    @Test
    public void testMaakAanDisabled() {
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
    public void testMakeNewClip() {
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
    public void testAddNewStandardOmroep() {
    	loginSpeciaalVf();
    	
    	Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	String omroep = "NPS";
		boolean visibleStandaardOmroep = overlayPage.isVisibleStandaardOmroep(omroep);
    	System.out.println("###" + visibleStandaardOmroep);
//		Assert.assertFalse(String.format("Standard omroep %s is not present", omroep), 
//				visibleStandaardOmroep);
//    	overlayPage.addStandaardOmroep(omroep);
//    	overlayPage.clickOpslaan();
//    	Sleeper.sleep(Duration.ofSeconds(5));
//    	Sleeper.sleep(5000);
    	
    	
//    	search.clickNew();
//    	AddNewObjectOverlayPage overlay = new AddNewObjectOverlayPage(driver);
//    	boolean omroepIsSelected = overlay.omroepIsSelected(omroep);
//    	System.out.println(omroepIsSelected);
//		Assert.assertTrue(String.format("Standard omroep %s is present", omroep), omroepIsSelected);
//    	overlay.close();
//    	
//    	logout();
    }
    
    @Test
    public void testAddTwoStandardOmroep() {
    	loginSpeciaalVf();
    
    	Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	String nps = "NPS";
		boolean visibleStandaardOmroep = overlayPage.isVisibleStandaardOmroep(nps);
		Assert.assertFalse(String.format("Standard omroep %s is not present", nps), 
				visibleStandaardOmroep);
		String vpro = "VPRO";
		visibleStandaardOmroep = overlayPage.isVisibleStandaardOmroep(vpro);
		Assert.assertFalse(String.format("Standard omroep %s is not present", vpro), 
				visibleStandaardOmroep);
    	overlayPage.addStandaardOmroep(nps);
    	overlayPage.addStandaardOmroep(vpro);
    	overlayPage.clickOpslaan();
    	search.clickNew();
    	
    	AddNewObjectOverlayPage overlay = new AddNewObjectOverlayPage(driver);
    	boolean omroepIsSelected = overlay.omroepIsSelected(nps);
		Assert.assertTrue(String.format("Standard omroep %s is present", nps), omroepIsSelected);
		omroepIsSelected = overlay.omroepIsSelected(vpro);
		Assert.assertTrue(String.format("Standard omroep %s is present", vpro), omroepIsSelected);
    	overlay.close();
    	
    	logout();
    }
    
    @Test
    public void testPersistStandardOmroep() {
    	loginSpeciaalVf();
    
    	Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	overlayPage.removeStandaardOmroep("VRPO");
    	overlayPage.addStandaardOmroep("NPS");
    	
    	overlayPage.clickOpslaan();
    	search.goToAccountInstellingen();
    	boolean omroepIsVisible = overlayPage.isVisibleStandaardOmroep("NPS");
		Assert.assertTrue(String.format("Standard omroep %s is present", "NPS"), omroepIsVisible);
    	
    	logout();
    	loginSpeciaalVf();
    	search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	overlayPage = new AccountSettingsOverlayPage(driver);
    	omroepIsVisible = overlayPage.isVisibleStandaardOmroep("NPS");
		Assert.assertTrue(String.format("Standard omroep %s is present", "NPS"), omroepIsVisible);
		overlayPage.clickOpslaan();
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
