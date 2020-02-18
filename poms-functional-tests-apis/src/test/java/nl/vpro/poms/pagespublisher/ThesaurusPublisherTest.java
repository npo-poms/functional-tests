package nl.vpro.poms.pagespublisher;

import lombok.extern.log4j.Log4j2;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.*;

import nl.vpro.api.client.pages.PageUpdateApiClient;
import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.PersonInterface;
import nl.vpro.domain.api.thesaurus.PersonResult;
import nl.vpro.domain.gtaa.GTAANewPerson;
import nl.vpro.domain.gtaa.GTAAPerson;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
class ThesaurusPublisherTest extends AbstractApiTest {

   private PageUpdateApiClient pageUpdateApiClient = PageUpdateApiClient.configured(
       CONFIG.env(),
       CONFIG.getProperties(Config.Prefix.npo_pageupdate_api)
   ).build();


    private static String givenName;
    private static String familyName = "Puk";
    private static String gtaaId;


    @Test
    @Order(1)
    void createPerson() {
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
    @Order(2)
    void newPersonArrivedInGTAA() {
        Assumptions.assumeThat(gtaaId).isNotNull();
        log.info("Getting person with id {}", gtaaId);
        GTAAPerson item = (GTAAPerson) clients.getThesaurusRestService().conceptStatus(gtaaId);
        log.info("{}", item);
        assertThat(item).isNotNull();
        assertThat(item.getGivenName()).isEqualTo(givenName);



    }

    //Test fails if there is no '.' added after givenName.
    @Disabled
    @Test
    @Order(3)
    void newPersonIsAlsoFindableInGTAA() {
        Assumptions.assumeThat(gtaaId).isNotNull();
        PersonResult persons = clients.getThesaurusRestService().findPersons(givenName, 100);
        assertThat(persons.getSize()).isGreaterThan(0);
        for (PersonInterface p : persons) {
            log.info("{}", p);
        }
    }

}
