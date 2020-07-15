package nl.vpro.poms;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.jackson2.Jackson2Mapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class ApiSearchTestHelper {
    private static Map<String, NpoApiClients> cache = new HashMap<>();

    public static <T> Stream<Arguments> getForms(NpoApiClients clients, String dir, Class<T> formClass, String... profiles) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Map.Entry<String, T>> forms = new ArrayList<>();
        {
            Resource[] resources = resolver.getResources("classpath*:" + dir + "*.json");

            for (Resource resource : resources) {
                try {
                    T form = Jackson2Mapper.getLenientInstance().readerFor(formClass).readValue(resource.getInputStream());
                    forms.add(new AbstractMap.SimpleEntry<>(resource.getFilename(), form));
                } catch (Exception ignored) {

                }
            }
        }
        {
            Resource[] resources = resolver.getResources("classpath*:" + dir + "*.xml");

            for (Resource resource : resources) {
                try {
                    T form = JAXB.unmarshal(resource.getInputStream(), formClass);
                    forms.add(new AbstractMap.SimpleEntry<>(resource.getFilename(), form));
                } catch (Exception ignored) {

                }
            }
        }
        List<Arguments> result = new ArrayList<>();
        for (MediaType mediaType : Arrays.asList(APPLICATION_XML_TYPE, APPLICATION_JSON_TYPE)) {

            if (profiles.length > 0) {
                for (String profile : profiles) {
                    for (Map.Entry<String, T> e : forms) {
                        String key = "" + profile + "/" + mediaType;
                        result.add(
                            Arguments.of(
                                e.getKey() + "/" + profile + "/" + mediaType.getSubtype(),
                                e.getValue(),
                                cache.computeIfAbsent(key,
                                    (k) -> clients.toBuilder()
                                        .profile(profile)
                                        .accept(mediaType)
                                        .toString((c) -> key)
                                        .build())
                            )
                        );
                    }
                }
            } else {
                for (Map.Entry<String, T> e : forms) {
                    String key = "" + mediaType;
                    result.add(
                        Arguments.of(
                            e.getKey() + "/" + mediaType.getSubtype(),
                            e.getValue(),
                            cache.computeIfAbsent(key, (k)->
                                clients.toBuilder()
                                    .accept(mediaType)
                                    .toString((c) -> key)
                                    .build()
                            )
                        )
                    );
                }
            }
        }

        return result.stream();
    }
}
