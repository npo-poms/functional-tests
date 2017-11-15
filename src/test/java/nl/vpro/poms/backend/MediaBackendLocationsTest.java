package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.update.LocationUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.DoAfterException;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeNoException;

/**
 * Tests whether adding and modifying locations via the POMS backend API works.
 *
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendLocationsTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final List<String> titles = new ArrayList<>();

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendLocationsTest.exception = t;
        }
    });

    private static Throwable exception = null;

    private static String firstTitle;

    @Before
    public void setup() {
        assumeNoException(exception);
    }

    @Test
    public void test01addLocation() {
        titles.add(title);
        firstTitle = title;
        LocationUpdate update = LocationUpdate.builder()
            .programUrl(programUrl(firstTitle))
            .build();
        backend.addLocationToProgram(update, MID);
    }


    @Test
    public void test10checkArrived() throws Exception {
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
    public void test11checkArrivedViaGetLocations() throws Exception {
        List<String> currentLocations = new ArrayList<>();
        waitUntil(ACCEPTABLE_DURATION,
            MID + " in backend with location " + titles,
            () -> {
                XmlCollection<LocationUpdate> update = backend.getBackendRestService().getLocations(null, MID, true);
                currentLocations.clear();
                currentLocations.addAll(update.stream().map(LocationUpdate::getProgramUrl).collect(Collectors.toList()));
                return currentLocations.containsAll(titles.stream().map(this::programUrl).collect(Collectors.toSet()));
            });

        assertThat(currentLocations).containsAll(titles.stream().map(this::programUrl).collect(Collectors.toSet()));
    }

    @Test
    public void test12updateLocation() throws IOException {
        String firstLocation = programUrl(firstTitle);
        LocationUpdate update = backend.getBackendRestService()
            .getLocations(null, MID, true).stream()
            .filter(l -> l.getProgramUrl().equals(firstLocation))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(MID + " has no location " + firstLocation));

        update.setProgramUrl(programUrl(title));
        titles.remove(firstTitle);
        titles.add(title);


        backend.addLocationToProgram(update, MID);
    }



    @Test
    public void test20addLocationToObject() {
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
    public void test21addLocationToObjectCheck() throws Exception {
        List<String> currentLocations = new ArrayList<>();
        waitUntil(ACCEPTABLE_DURATION,
            MID + " in backend with location " + titles,
            () -> {
                    XmlCollection<LocationUpdate> update = backend.getBackendRestService().getLocations(null, MID, true);
                currentLocations.clear();
                currentLocations.addAll(update.stream().map(LocationUpdate::getProgramUrl).collect(Collectors.toList()));
                Set<String> exprectedLocations = titles.stream().map(this::programUrl).collect(Collectors.toSet());
                return currentLocations.containsAll(exprectedLocations);
            });

    }


    @Test(expected = Exception.class)
    public void test40addInvalidLocationToObject() throws Exception {
        backend.doValidated(() -> {
            LocationUpdate location = LocationUpdate
                .builder()
                .programUrl("http:ongeldigeurl")
                .build();

            ProgramUpdate update = backend.get(MID);
            update.getLocations().add(location);
            backend.set(update);
        });
    }


    @Test
    public void test98Cleanup() throws Exception {
        backend.getBrowserCache().clear();
        ProgramUpdate update = backend.get(MID);
        log.info("Removing locations " + update.getLocations());
        update.getLocations().clear();
        backend.set(update);
    }


    @Test
    public void test99CleanupCheck() throws Exception {
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
