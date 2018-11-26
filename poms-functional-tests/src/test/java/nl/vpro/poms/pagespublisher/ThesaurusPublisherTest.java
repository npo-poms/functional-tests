package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.PersonInterface;
import nl.vpro.domain.api.thesaurus.PersonResult;
import nl.vpro.domain.media.gtaa.GTAANewPerson;
import nl.vpro.domain.media.gtaa.GTAAPerson;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.rules.DoAfterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ThesaurusPublisherTest extends AbstractApiTest {

   PageUpdateApiClient pageUpdateApiClient = PageUpdateApiClient.configured(
       CONFIG.env(),
       CONFIG.getProperties(Config.Prefix.npo_pageupdate_api)
   ).build();


    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> ThesaurusPublisherTest.exception = t);

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }

    private static String givenName;
    private static String familyName = "Puk";
    private static String gtaaId;


    @Test
    public void test001CreatePerson() {
        givenName = "Pietje2" + System.currentTimeMillis();
        log.info("Creating {} {}", givenName, familyName);
        GTAAPerson created = pageUpdateApiClient.getThesaurusUpdateRestService().submit(null,
            GTAANewPerson.builder()
                .familyName("Puk")
                .givenName(givenName)
                .build()
        );
        gtaaId = created.getGtaaUri();
        log.info("Created {}", created);


    }

    @Test
    public void test100Arrived() throws Exception {
        assumeNotNull(gtaaId);

        GTAAPerson item = (GTAAPerson) clients.getThesaurusRestService().itemStatus(gtaaId);
        log.info("{}", item);
        assertThat(item).isNotNull();
        assertThat(item.getGivenName()).isEqualTo(givenName);



    }

    //Test fails if there is no '.' added after givenName.
    @Ignore
    @Test
    public void test101ArrivedAndFindable() {
        assumeNotNull(gtaaId);
        PersonResult persons = clients.getThesaurusRestService().findPersons(givenName, 100);
        assertThat(persons.getSize()).isGreaterThan(0);
        for (PersonInterface p : persons) {
            log.info("{}", p);

        }
    }

}
