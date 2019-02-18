package nl.vpro.poms.config;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.api.client.utils.Config;
import nl.vpro.rules.TestMDC;

@Slf4j
public abstract class Webtest {


    public static final String    MID                = "WO_VPRO_025057";


    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.MINUTES);


    @Rule
    public TestMDC testMDC = new TestMDC();



    protected static ChromeDriver driver;

    protected static NgWebDriver ngWebDriver;


    protected static String CHROME_DRIVER_VERSION = "2.41"; // null is 'newest'.

    protected static final Config CONFIG = new Config("npo-functional-tests.properties", "npo-browser-tests.properties");


    protected static void login(String address, String userName, String password) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.setHeadless(Boolean.parseBoolean(CONFIG.getProperties().get("headless")));
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get(address);


        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.name("submit")).click();
        ngWebDriver = new NgWebDriver(driver);

    }

    public static void loginVPROand3voor12() {
        String url = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");
        log.info("poms {}", url);
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        Assume.assumeNotNull(user, password);
        login(
            url,
            user,
            password);
    }

    public static void loginGtaaBrowserTest() {
        ChromeOptions options = new ChromeOptions();
        String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/";
        options.setHeadless(false);
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");

        login(url, user, password);

    }

    protected static void getDriver() throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            String version = CHROME_DRIVER_VERSION;
            if (version  == null) {
                HttpResponse getVersion = client.execute(new HttpGet("https://chromedriver.storage.googleapis.com/LATEST_RELEASE"));

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                getVersion.getEntity().writeTo(bytes);
                version = new String(bytes.toByteArray());
            }

            String osName = System.getProperty("os.name");
            String file = "chromedriver_linux64.zip";
            if (osName.startsWith("Mac")) {
                file = "chromedriver_mac64.zip";
            } else if (osName.startsWith("Windows")) {
                file = "chromedriver_win32.zip";
            }

            log.info("Using {} {}", version, file);

            File dest = new File(System.getProperty("java.io.tmpdir"), version + File.separator + "chromedriver");
            dest.getParentFile().mkdirs();
            if (! dest.exists()) {
                String url = "https://chromedriver.storage.googleapis.com/" + version + "/" + file;
                HttpResponse getZip = client.execute(new HttpGet(url));
                try (InputStream inputStream = getZip.getEntity().getContent();
                     OutputStream out = new FileOutputStream(dest)) {
                    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    log.info("Reading {} ({} bytes)", nextEntry, nextEntry.getSize());
                    IOUtils.copy(zipInputStream, out);
                    dest.setExecutable(true);
                    log.info("Downloaded {} -> {}", url, dest);
                }

            }
            log.info("Using driver {}", dest);
            System.setProperty("webdriver.chrome.driver", dest.getAbsolutePath());
        }

    }

    @BeforeClass
    public static void setupDriver() throws IOException {
        getDriver();
    }


    /**
     * Tear down.
     *
     */
    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            if (Boolean.parseBoolean(CONFIG.getProperties().get("headless"))) {
                driver.quit();
            }
        } else {
            log.warn("No driver set");
        }
    }

}
