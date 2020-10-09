package nl.vpro.poms.resources;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.classification.ClassificationService;
import nl.vpro.domain.classification.URLClassificationServiceImpl;
import nl.vpro.junit.extensions.TestMDC;

import static nl.vpro.testutils.Utils.CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
  */
@Log4j2
@ExtendWith(TestMDC.class)
class ClassificationTest {


    @Test
    public void testClassification() {
        ClassificationService service = new URLClassificationServiceImpl(CONFIG.requiredOption(Config.Prefix.npo_pageupdate_api, "baseUrl") + "/schema/classification");
        log.info("Found service {}", service);
        assertThat(service.getTerm("3.0.7").getName()).isEqualTo("Proprietary Cinema genres");


    }
}
