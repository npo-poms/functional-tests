package nl.vpro.poms.resources;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.xml.sax.SAXException;

import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.api.client.utils.Config;
import nl.vpro.util.IntegerVersion;

import static nl.vpro.poms.AbstractApiTest.CONFIG;

/**
 * @author Michiel Meeuwissen
 */
@Slf4j
public class SchemaTest {




    @Test
    public void testUpdateSchema() throws IOException, SAXException {
        String baseUrl = CONFIG.requiredOption(Config.Prefix.poms, "baseUrl");
        SchemaFactory factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema xsdSchema = factory.newSchema(
            new URL(baseUrl + "/schema/update/vproMediaUpdate.xsd"));
        Validator xsdValidator = xsdSchema.newValidator();

        ProgramUpdate update = ProgramUpdate.create(MediaTestDataBuilder.program()
            .withEverything(IntegerVersion.of(5, 10))
            .build());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(update, out);
        log.info(new String(out.toByteArray()));

        Source streamSource = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        xsdValidator.validate(streamSource);


    }

    @Test
    public void testSchema() throws IOException, SAXException {
        String baseUrl = CONFIG.requiredOption(Config.Prefix.poms, "baseUrl");
        SchemaFactory factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL url = new URL(baseUrl + "/schema/vproMedia.xsd");
        log.info("{}", url);
        Schema xsdSchema = factory.newSchema(url);
        //Schema xsdSchema = factory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream("/nl/vpro/domain/media/vproMedia.xsd")));
        Validator xsdValidator = xsdSchema.newValidator();

        Program program = MediaTestDataBuilder.program().withEverything().build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(program, out);
        log.info(new String(out.toByteArray()));

        Source streamSource = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        xsdValidator.validate(streamSource);


    }
}
