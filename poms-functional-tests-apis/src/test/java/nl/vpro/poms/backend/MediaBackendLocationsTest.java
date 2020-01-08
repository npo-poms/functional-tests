package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.domain.media.update.LocationUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.test.jupiter.NoAbort;

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
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
@ExtendWith(AbortOnException.class)
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
    void test01addLocation() {
        titles.add(title);
        firstTitle = title;
        LocationUpdate update = LocationUpdate.builder()
            .programUrl(programUrl(firstTitle))
            .build();
        backend.addLocationToProgram(update, MID);
    }


    @Test
    void test10checkArrived() {
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
    void test11checkArrivedViaGetLocations() {
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
    void test12updateLocation() throws IOException {
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
    void test20addLocationToObject() {
        titles.add(title);
        LocationUpdate location = LocationUpdate
            .builder()
            .programUrl(programUrl(title))
            .build();

        ProgramUpdate update = backend.get(MID);
        update.getLocations().add(location);
        backend.set(update);
        JAXB.marshal(update, LoggerOutputStream.debug(log));
    }


    @Test
    void test21addLocationToObjectCheck() {
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
    void test40addInvalidLocationToObject() {
        Assertions.assertThrows(Exception.class, () -> {
            backend.doValidated(() -> {
                LocationUpdate location = LocationUpdate
                    .builder()
                    .programUrl("http:ongeldigeurl")
                    .build();

                ProgramUpdate update = backend.get(MID);
                update.getLocations().add(location);
                backend.set(update);
            });
        });
    }


    @Test
    @NoAbort
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
