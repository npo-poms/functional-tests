package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.poms.Config;
import nl.vpro.poms.DoAfterException;
import nl.vpro.rs.thesaurus.update.NewPerson;
import nl.vpro.rs.thesaurus.update.NewPersonRequest;

import static org.junit.Assume.assumeNoException;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ThesaurusPublisherTest extends AbstractApiTest {

   PageUpdateApiClient pageUpdateApiClient = PageUpdateApiClient.configured(
       Config.env(),Config.getProperties(Config.Prefix.pageupdateapi)
        ).build();


    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        ThesaurusPublisherTest.exception = t;
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }


    @Test
    public void test001CreatePerson() throws UnsupportedEncodingException {
        pageUpdateApiClient.getThesaurusUpdateRestService().submitSigned(
            NewPersonRequest.builder().person(
                NewPerson.builder().familyName("Puk").givenName("Pietje").build()
            ).build());

    }

    @Test
    public void test100Arrived() throws Exception {

    }

}
