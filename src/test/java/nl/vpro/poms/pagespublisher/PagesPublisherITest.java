package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.api.client.utils.PageUpdateApiUtil;
import nl.vpro.api.client.utils.PageUpdateRateLimiter;
import nl.vpro.api.client.utils.Result;
import nl.vpro.domain.page.Page;
import nl.vpro.domain.page.Section;
import nl.vpro.domain.page.update.EmbedUpdate;
import nl.vpro.domain.page.update.PageUpdate;
import nl.vpro.domain.page.update.PageUpdateBuilder;
import nl.vpro.domain.page.update.PortalUpdate;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.poms.Config;
import nl.vpro.poms.DoAfterException;
import nl.vpro.poms.Utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PagesPublisherITest extends AbstractApiTest {


    static PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            Config.env(), Config.getProperties(Config.Prefix.pageupdateapi)
        ).build(),
        PageUpdateRateLimiter.builder().build()
    );

    static {
        log.info("Using {}", util);
    }

    static PageUpdate article;

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        PagesPublisherITest.exception = t;
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }


    @Test
    public void test001CreateOrUpdatePage() throws UnsupportedEncodingException {

        PortalUpdate portal = new PortalUpdate("WETENSCHAP24", "http://test.poms.nl");
        portal.setSection(new Section("/" + name.getMethodName(), "Display name " + name.getMethodName()));
        article =
            PageUpdateBuilder.article("http://test.poms.nl/" + URLEncoder.encode(name.getMethodName() + LocalDate.now(), "UTF-8"))
                .broadcasters("VPRO")
                .title(title)
                .embeds(EmbedUpdate.builder().midRef("WO_VPRO_025057").title("leuke embed").description("embed in " + title).build())
                .portal(portal)
            .build();

        Result result = util.save(article);
        System.out.println(result);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() throws Exception {
        assumeNotNull(article);

        PageUpdate update = Utils.waitUntil(Duration.ofMinutes(1), () ->
            util.get(article.getUrl()),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());


        Page page = Utils.waitUntil(Duration.ofMinutes(1), () ->
            pageUtil.load(article.getUrl())[0], p -> Objects.equals(p.getTitle(), article.getTitle())
        );

        assertThat(page.getTitle()).isEqualTo(article.getTitle());
        assertThat(page.getEmbeds()).hasSize(1);
        assertThat(page.getEmbeds().get(0).getMedia()).isNotNull();
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo("testclip michiel");
        assertThat(page.getEmbeds().get(0).getTitle()).isEqualTo("leuke embed");
        assertThat(page.getEmbeds().get(0).getDescription()).isEqualTo("embed in " + article.getTitle());


    }
}
