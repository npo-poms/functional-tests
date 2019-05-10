package nl.vpro.poms.selenium.poms.itemizer;

import nl.vpro.poms.selenium.pages.OmroepVideoDetailInfoPage;
import nl.vpro.poms.selenium.pages.OmroepVideoPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemizerTest extends AbstractTest {

    private OmroepVideoDetailInfoPage objVideoInfoPage;
    private String firstSegmentName;
    private String secondSegmentName;


    @Before
    public void setup() {
        loginSpeciaalVf();
    }

    @After
    public void teardown() {
        super.logout();
    }

    @Test
    public void itemizerTest() {
        Search searchPage = new Search(driver);
        //Verify home page
        assertThat(searchPage.getCurrentUser()).contains("VF Specialisterren");

        //search for the Tegenlicht episode 'Transitie'
        //searchPage.enterQuery("Transitie");
        searchPage.selectOptionFromMenu("Criteria", "Mag schrijven");
        searchPage.selectOptionFromMenu("Criteria", "Beschikbaar op streaming platform");
        searchPage.clickRow(1);

        //add the first segment
        OmroepVideoPage objVideoPage = new OmroepVideoPage(driver);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateStr = dateFormat.format(date);
        firstSegmentName = "Segment1" + dateStr;
        objVideoPage.addSegment(firstSegmentName, "Segment beschrijving 1", 7000, 13000);
        objVideoPage.clickBewaarEnSluitSegment();

        //validate the first segment is shown on the video page with correct time values
        assertThat(objVideoPage.checkSegmentNameIsVisible (firstSegmentName));
        assertThat(objVideoPage.checkDurationOfSegmentApprox(firstSegmentName,13));
        assertThat(objVideoPage.checkStartTimeOfSegmentApprox(firstSegmentName, 7));
        assertThat(objVideoPage.checkEndTimeOfSegmentApprox(firstSegmentName, 20));

        //add the second segment
        dateStr = dateFormat.format(date);
        secondSegmentName = "Segment2" + dateStr;
        objVideoPage.addSegment(secondSegmentName, "Segment beschrijving 2", 7000, 25000);
        objVideoPage.clickBewaarEnSluitSegment();

        //validate the second segment is shown on the video page with correct time values
        assertThat(objVideoPage.checkSegmentNameIsVisible (secondSegmentName));
        assertThat(objVideoPage.checkDurationOfSegmentApprox(secondSegmentName,25));
        assertThat(objVideoPage.checkStartTimeOfSegmentApprox(secondSegmentName, 7));
        assertThat(objVideoPage.checkEndTimeOfSegmentApprox(secondSegmentName, 32));

        //change the start time of the first segment and validate the new time values
        objVideoPage.changeSegmentStart(firstSegmentName, 12000);
        objVideoPage.clickBewaarEnSluitSegment();
        assertThat(objVideoPage.checkDurationOfSegmentApprox(firstSegmentName,8));
        assertThat(objVideoPage.checkStartTimeOfSegmentApprox(firstSegmentName, 12));
        assertThat(objVideoPage.checkEndTimeOfSegmentApprox(firstSegmentName, 20));

        //change the end time of the first segment and validate the new time values
        objVideoPage.changeSegmentEnd(firstSegmentName, 30000);
        objVideoPage.clickBewaarEnSluitSegment();
        assertThat(objVideoPage.checkDurationOfSegmentApprox(firstSegmentName,18));
        assertThat(objVideoPage.checkStartTimeOfSegmentApprox(firstSegmentName, 12));
        assertThat(objVideoPage.checkEndTimeOfSegmentApprox(firstSegmentName, 30));

        //change the start time of the second segment by using the text field and validate the new time values
        objVideoPage.changeSegmentStartTextfield(secondSegmentName, "00:00:09.000");
        objVideoPage.clickBewaarEnSluitSegment();
        assertThat(objVideoPage.checkDurationOfSegmentApprox(secondSegmentName,23));
        assertThat(objVideoPage.checkStartTimeOfSegmentApprox(secondSegmentName, 9));
        assertThat(objVideoPage.checkEndTimeOfSegmentApprox(secondSegmentName, 32));

        //change the end time of the second segment by using the text field and validate the new time values
        objVideoPage.changeSegmentEndTextfield(secondSegmentName, "00:00:49.000");
        objVideoPage.clickBewaarEnSluitSegment();
        assertThat(objVideoPage.checkDurationOfSegmentApprox(secondSegmentName,40));
        assertThat(objVideoPage.checkStartTimeOfSegmentApprox(secondSegmentName, 9));
        assertThat(objVideoPage.checkEndTimeOfSegmentApprox(secondSegmentName, 49));


        //add a picture to the second segment and validate it is present
    /*    System.out.println("picture present? 1 : " + objVideoPage.isPicturePresent());
        objVideoPage.createPicture(secondSegmentName);
        Assert.assertTrue(objVideoPage.isPicturePresent()); */


        //open a detail page of the second segment
        objVideoPage.openSegmentDetailInfoPage(secondSegmentName);

        //check if an urn is present in the detail page
        objVideoInfoPage = new OmroepVideoDetailInfoPage(driver);
        // System.out.println("urn present: " + objVideoInfoPage.checkUrnPresent());
        assertThat(objVideoInfoPage.checkUrnPresent());

        //fill data in the other fields on the detail page
        dateStr = dateFormat.format(date);
        objVideoInfoPage.enterDataOtherFields(dateStr);
    }
}
