package nl.vpro.poms.selenium.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import nl.vpro.poms.selenium.util.WebDriverUtil;


public class OmroepVideoPage extends AbstractPage {


    public final int WAIT_TIME_CLICK_VIDEO_LONG = 1300;
    public final int WAIT_TIME_CLICK_VIDEO_SHORT = 100;

    public final int DX_MOVIESEEKBAR = -145;

    public final int DX_MOVIESEEKBAR_PICTURE = 150;

    private By userName = By.xpath("//div[@class='header-account-details']/descendant::span[1]");
    private By searchField = By.id("query");
    private By searchButton = By.id("submit");
    private By clearButton = By.xpath("//button[normalize-space(.)='Wissen']");
    //private By allTitles = By.xpath("//table//tr");
    private By allTitles = By.xpath("(//table//tr)//td[3]//span");
    private By allSubTitles = By.xpath("(//table//tr)//td[4]//span");
    private By allTypes = By.xpath("(//table//tr)//td[6]//span");
    private By videoPlayer = By.xpath("//div[@aria-label='Video Player'][1]");

    private By addSegment = By.xpath("//button[normalize-space(text())='Segment toevoegen']");

    private By titleSegment = By.xpath("//input[@name='inputTitle']");

    private By startSegment = By.xpath("//input[@id='inputStart']");
    private By stopSegment = By.xpath("//input[@id='inputStop' ]");

    private By descriptionSegment = By.xpath("//textarea[@id='inputDescription']");

    private By playButton = By.xpath("//div[@id='playPauseBtn']");

    private By seekbar = By.xpath("//input[@id='seekbar']");

    private By startPointSegment = By.xpath("//span[normalize-space(.)='Zet huidige positie als starttijd']");
    private By endPointSegment = By.xpath("//span[normalize-space(.)='Zet huidige positie als stoptijd']");

    private By saveSegment = By.xpath("//button[normalize-space(.)='Bewaar en sluit']");

    private By allSegments = By.xpath("//tr[contains(@ng-repeat,'segment')]/descendant::span[@ng-switch-when='mainTitle']/child::span");

    private By pictureButton = By.xpath("//div[@class='canvas-grab']");
    private By pictureLocation = By.xpath("//div[@ng-if='still']");

    public OmroepVideoPage(WebDriverUtil driver) {
        super(driver);
    }


    public void clickSegmentToevoegen() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement element1 = driver.findElement(addSegment);

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element1);

        webDriverUtil.waitAndClick(addSegment);
    }

    public void enterTitelSegmentPopup(String textToEnter) {
        driver.findElement(titleSegment).sendKeys(textToEnter);
    }

    public void enterStartTimeSegmentPopup(String textToEnter) {
        driver.findElement(startSegment).clear();
        driver.findElement(startSegment).sendKeys(textToEnter);
    }

    public void enterStopTimeSegmentPopup(String textToEnter) {
        driver.findElement(stopSegment).clear();
        driver.findElement(stopSegment).sendKeys(textToEnter);
    }

    public void enterBeschrijvingSegmentPopup(String textToEnter) {
        driver.findElement(descriptionSegment).sendKeys(textToEnter);
    }

    public void clickPictureButton() {
        driver.findElement(pictureButton).click();
    }

    public void clickPlayPauseSegmentButton() {
        WebElement element = driver.findElement(playButton);

        // Configure the Action
   /*     Actions action = new Actions(driver);
        //Focus to element
        action.moveToElement(element).perform();
        // To click on the element
        action.moveToElement(element).click().perform();

        try {
            Thread.sleep(2000);
        }catch(InterruptedException ex){
            ex.printStackTrace();
        } */

        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void clickStartPuntSegment() {
        driver.findElement(startPointSegment).click();
    }

    public void clickEindPuntSegment() {
        driver.findElement(endPointSegment).click();
    }

    public void clickBewaarEnSluitSegment() {
        WebElement element = driver.findElement(saveSegment);

        // Configure the Action
        Actions action = new Actions(driver);
        //Focus to element
        action.moveToElement(element).perform();
        // To click on the element
        action.moveToElement(element).click().perform();

        try {
            Thread.sleep(25000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void addSegment(String title, String description, int waitTime1, int waitTime2) {
        clickSegmentToevoegen();

        enterTitelSegmentPopup(title);
        enterBeschrijvingSegmentPopup(description);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickPlayPauseSegmentButton();
        moveSeekBar(DX_MOVIESEEKBAR);

        try {
            Thread.sleep(waitTime1 - WAIT_TIME_CLICK_VIDEO_LONG);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickStartPuntSegment();

        try {
            Thread.sleep(waitTime2 - WAIT_TIME_CLICK_VIDEO_SHORT);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickEindPuntSegment();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void changeSegmentStart(String title, int waitTime1) {

        openSegmentItemize(title);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }


        clickPlayPauseSegmentButton();
        moveSeekBar(DX_MOVIESEEKBAR);

        try {
            Thread.sleep(waitTime1 - WAIT_TIME_CLICK_VIDEO_LONG);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickStartPuntSegment();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public void changeSegmentEnd(String title, int waitTime1) {
        openSegmentItemize(title);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickPlayPauseSegmentButton();
        moveSeekBar(DX_MOVIESEEKBAR);

        try {
            Thread.sleep(waitTime1 - WAIT_TIME_CLICK_VIDEO_LONG);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickEindPuntSegment();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void changeSegmentStartEnd(String title, int waitTime1, int waitTime2) {
        openSegmentItemize(title);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickPlayPauseSegmentButton();


        try {
            Thread.sleep(waitTime1);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickStartPuntSegment();

        try {
            Thread.sleep(waitTime2);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        clickEindPuntSegment();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void changeSegmentStartTextfield(String title, String startTime) {
        openSegmentItemize(title);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        enterStartTimeSegmentPopup(startTime);

    }

    public void changeSegmentEndTextfield(String title, String stopTime) {
        openSegmentItemize(title);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        enterStopTimeSegmentPopup(stopTime);

    }

    public List<WebElement> getAllSegments() {
        return driver.findElements(allSegments);
    }

    public boolean checkDurationOfSegmentApprox(String segmentTitle, int durationSeconds) {

        By durationlocation = By.xpath("(//span[@title='" + segmentTitle + "'])[1]/following::span[@ng-switch-when='duration'][1]/descendant::span");

        String duration = driver.findElement(durationlocation).getAttribute("title");

        System.out.println("real duration for segmentitle: " + segmentTitle + " --- " + duration);

        return timeElementApproxEqual(duration, durationSeconds);
    }

    public boolean checkStartTimeOfSegmentApprox(String segmentTitle, int startTimeSeconds) {

        By startLocation = By.xpath("(//span[@title='" + segmentTitle + "'])[1]/following::span[@ng-switch-when='start'][1]/descendant::span");

        String start = driver.findElement(startLocation).getAttribute("title");

        System.out.println("real start time for segmentitle: " + segmentTitle + " --- " + start);

        return timeElementApproxEqual(start, startTimeSeconds);

    }

    public boolean checkEndTimeOfSegmentApprox(String segmentTitle, int endTimeSeconds) {

        By startLocation = By.xpath("(//span[@title='" + segmentTitle + "'])[1]/following::span[@ng-switch-when='stop'][1]/descendant::span");

        String stop = driver.findElement(startLocation).getAttribute("title");

        System.out.println("real stop time for segmentitle: " + segmentTitle + " --- " + stop);

        return timeElementApproxEqual(stop, endTimeSeconds);

    }

    /**
     * Returns whether the expected time (timeExpectedSeconds) is approxomately equal to the real time timeFound.
     * In this case, it is allowed to deviate 1 second up or down.
     *
     * @param timeFound
     * @param timeExpectedSeconds
     * @return True when the timeExpectedSeconds is at most 1 second different from timeFound
     */
    public boolean timeElementApproxEqual(String timeFound, int timeExpectedSeconds) {

        String expected = "00:00:0";
        String expectedUp = "00:00:0";
        String expectedDown = "00:00:0";

        if (timeExpectedSeconds >= 10) {
            expected = "00:00:";
            expectedUp = "00:00:";
            expectedDown = "00:00:";
        }

        expected = expected + Integer.toString(timeExpectedSeconds);
        expectedUp = expectedUp + Integer.toString(timeExpectedSeconds + 1);
        expectedDown = expectedDown + Integer.toString(timeExpectedSeconds - 1);

        System.out.println("expected duration for segmenttitle: " + expected);


        if (timeFound.equals(expected)) {
            return true;
        } else if (timeFound.equals(expectedUp) || timeFound.equals(expectedDown)) {
            return true;
        } else {
            return false;
        }

    }

    public boolean checkSegmentNameIsVisible(String segmentName) {
        List<WebElement> allSegments = getAllSegments();

        System.out.println("allsegments size: " + allSegments.size());

        for (int v = 0; v < allSegments.size(); v++) {
            System.out.println(allSegments.get(v).getAttribute("title"));
            if (allSegments.get(v).getAttribute("title").equals(segmentName)) ;
            return true;
        }
        return false;
    }

    public void moveSeekBar(int dx) {

        WebElement element = driver.findElement(seekbar);

        System.out.println("x value: " + element.getLocation().getX());
        System.out.println("y value: " + element.getLocation().getY());

        // Configure the Action
        Actions action = new Actions(driver);
        action.moveToElement(element, dx, 0);
        action.moveToElement(element, dx, 0).click().perform();

        //Focus to element
        //   action.moveToElement(element).perform();
        // To click on the element
        //action.moveToElement(element).dragAndDropBy(element, dx, 0);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public void createPicture(String segmentTitle) {
        openSegmentItemize(segmentTitle);
        moveSeekBar(DX_MOVIESEEKBAR_PICTURE);
        clickPictureButton();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public boolean isPicturePresent() {
        return (driver.findElements(pictureLocation).size() > 0);
    }

    /**
     * Opens the popup on which the video/segment itself can be edited (start time, etc)
     *
     * @param segmentName
     */
    public void openSegmentItemize(String segmentName) {

        WebElement element1 = driver.findElement(By.xpath("//span[@title='" + segmentName + "'][1]"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element1);

        // Configure the Action
        Actions action1 = new Actions(driver);
        //Focus to element
        action1.moveToElement(element1).perform();

        WebElement element = driver.findElement(By.xpath("((//span[@title='" + segmentName + "'][1])/following::button[@title='itemize'])[1]"));

        Actions action = new Actions(driver);
        action.doubleClick(element).perform();
    }

    /**
     * Opens the video/segment detail info page
     *
     * @param segmentName
     */
    public void openSegmentDetailInfoPage(String segmentName) {
        WebElement element1 = driver.findElement(By.xpath("//span[@title='" + segmentName + "'][1]"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element1);

        // Configure the Action
        Actions action1 = new Actions(driver);
        //Focus to element
        action1.moveToElement(element1).perform();

        WebElement element = driver.findElement(By.xpath("((//span[@title='" + segmentName + "'][1])/following::button[@title='edit segment'])[1]"));

        Actions action = new Actions(driver);
        action.doubleClick(element).perform();

    }
}
