package nl.vpro.poms.resources;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import nl.vpro.domain.classification.ClassificationService;
import nl.vpro.domain.classification.URLClassificationServiceImpl;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.api.client.utils.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
  */
@Slf4j
public class ClassificationTest {


    @Test
    public void testClassification() {
        ClassificationService service = new URLClassificationServiceImpl(AbstractApiTest.CONFIG.requiredOption(Config.Prefix.npo_pageupdate_api, "baseUrl") + "/schema/classification");
        log.info("Found service {}", service);
        assertThat(service.getTerm("3.0.7").getName()).isEqualTo("Proprietary Cinema genres");


    }
}
