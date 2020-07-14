package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import nl.vpro.poms.Require;
import org.junit.jupiter.api.*;

import nl.vpro.domain.media.update.LocationUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.logging.Log4j2OutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;


/*
 * 2018-08-15
 * 5.9-SNAPSHOT @ dev : allemaal ok

 */


/**
 * Tests whether adding and modifying locations via the POMS backend API works.
 *
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class MediaBackendLocationsTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final List<String> titles = new ArrayList<>();

    private static String firstTitle;

/*

    @Test
    public void testAddLocation() {
        titles.add(title);
        firstTitle = title;
        LocationUpdate update = LocationUpdate.builder()
            .programUrl(programUrl(firstTitle))
            .build();
        backend.addLocationToProgram(update, "WO_VPRO_3250811");
    }

*/



    @Test
    @Order(1)
    @Require.Needs(MID)
    void addLocation() {
        titles.add(title);
        firstTitle = title;
        LocationUpdate update = LocationUpdate.builder()
            .programUrl(programUrl(firstTitle))
            .build();
        backend.addLocationToProgram(update, MID);
    }


    @Test
    @Order(2)
    void checkArrivedLocation() {
        List<String> currentLocations = new ArrayList<>();
        waitUntil(ACCEPTABLE_DURATION,
            MID + " in backend with location " + titles,
            () -> {
                ProgramUpdate update = backend.get(MID);
                currentLocations.clear();
                currentLocations.addAll(update.getLocations().stream().map(LocationUpdate::getProgramUrl).collect(Collectors.toList()));
                return currentLocations.containsAll(titles.stream().map(this::programUrl).collect(Collectors.toSet()));
            });

        assertThat(currentLocations).containsAll(titles.stream().map(this::programUrl).collect(Collectors.toSet()));
    }

    @Test
    @Order(3)
    void checkArrivedViaGetLocations() {
        List<String> currentLocations = new ArrayList<>();
        waitUntil(ACCEPTABLE_DURATION,
            MID + " in backend with location " + titles,
            () -> {
                XmlCollection<LocationUpdate> update = backend.getBackendRestService().getLocations(null, MID, true, null);
                currentLocations.clear();
                currentLocations.addAll(update.stream().map(LocationUpdate::getProgramUrl).collect(Collectors.toList()));
                return currentLocations.containsAll(titles.stream().map(this::programUrl).collect(Collectors.toSet()));
            });

        assertThat(currentLocations).containsAll(titles.stream().map(this::programUrl).collect(Collectors.toSet()));
    }

    @Test
    @Order(10)
    void updateLocation() throws IOException {
        String firstLocation = programUrl(firstTitle);
        LocationUpdate update = backend.getBackendRestService()
            .getLocations(null, MID, true, null).stream()
            .filter(l -> l.getProgramUrl().equals(firstLocation))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(MID + " has no location " + firstLocation));

        update.setProgramUrl(programUrl(title));
        titles.remove(firstTitle);
        titles.add(title);


        backend.addLocationToProgram(update, MID);
    }



    @Test
    @Order(11)
    void addLocationToObject() {
        titles.add(title);
        LocationUpdate location = LocationUpdate
            .builder()
            .programUrl(programUrl(title))
            .build();

        ProgramUpdate update = backend.get(MID);
        update.getLocations().add(location);
        backend.set(update);
        JAXB.marshal(update, Log4j2OutputStream.debug(log));
    }


    @Test
    @Order(20)
    void addLocationToObjectCheck() {
        List<String> currentLocations = new ArrayList<>();
        waitUntil(ACCEPTABLE_DURATION,
            MID + " in backend with location " + titles,
            () -> {
                    XmlCollection<LocationUpdate> update = backend.getBackendRestService().getLocations(null, MID, true, null);
                currentLocations.clear();
                currentLocations.addAll(update.stream().map(LocationUpdate::getProgramUrl).collect(Collectors.toList()));
                Set<String> exprectedLocations = titles.stream().map(this::programUrl).collect(Collectors.toSet());
                return currentLocations.containsAll(exprectedLocations);
            });

    }


    @Test
    @Order(40)
    void addInvalidLocationToObject() {
        Assertions.assertThrows(Exception.class, () ->
            backend.doValidated(() -> {
                LocationUpdate location = LocationUpdate
                    .builder()
                    .programUrl("http:ongeldigeurl")
                    .build();

                ProgramUpdate update = backend.get(MID);
                update.getLocations().add(location);
                backend.set(update);
            }));
    }


    @Test
    @AbortOnException.NoAbort
    void test98Cleanup() {
        backend.getBrowserCache().clear();
        ProgramUpdate update = backend.get(MID);
        log.info("Removing locations " + update.getLocations());
        update.getLocations().clear();
        backend.set(update);
    }


    @Test
    void test99CleanupCheck() {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        waitUntil(ACCEPTABLE_DURATION,
            MID + " has no locations any more",
            () -> {
                update[0] = backend.get(MID);
                return update[0].getLocations().isEmpty();
            });
        assertThat(update[0].getLocations()).isEmpty();
    }


    private String programUrl(String title) {
        return "http://www.vpro.nl/" + title;
    }


}
