package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.api.client.utils.PageUpdateApiUtil;
import nl.vpro.api.client.utils.PageUpdateRateLimiter;
import nl.vpro.api.client.utils.Result;
import nl.vpro.domain.page.Page;
import nl.vpro.domain.page.update.PageUpdate;
import nl.vpro.domain.page.update.PageUpdateBuilder;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.poms.Config;
import nl.vpro.poms.Utils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PagesPublisherITest extends AbstractApiTest {


    static PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            Config.env(), Config.getProperties(Config.Prefix.pageupdateapi)).build(),
        PageUpdateRateLimiter.builder().build()
    );

    static PageUpdate article;


    @Test
    public void test001CreateOrUpdatePage() throws UnsupportedEncodingException {
        article =
            PageUpdateBuilder.article("http://test.poms.nl/" + URLEncoder.encode(name.getMethodName() + LocalDate.now(), "UTF-8"))
                .broadcasters("VPRO")
                .title(title)
            .build();

        Result result = util.save(article);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() throws Exception {
        Page page = Utils.waitUntil(Duration.ofMinutes(1), () -> pageUtil.load(article.getUrl())[0], p -> Objects.equals(p.getTitle(), article.getTitle()));

        assertThat(page.getTitle()).isEqualTo(article.getTitle());

    }
}
