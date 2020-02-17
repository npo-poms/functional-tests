package nl.vpro.poms.resources;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xml.sax.SAXException;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.junit.extensions.TestMDC;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.AbstractApiTest;

import static nl.vpro.poms.AbstractApiTest.CONFIG;
import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * Checks whether the objects 'with everything' indeed validate against the XSD's we provide publicly.
 * @author Michiel Meeuwissen
 */
@Log4j2
@ExtendWith(TestMDC.class)
class SchemaTest {

    @Test
    public void testUpdateSchema() throws IOException, SAXException {


        String baseUrl = CONFIG.requiredOption(Config.Prefix.poms, "baseUrl");
        URL url =  new URL(baseUrl + "/schema/update/vproMediaUpdate.xsd");
        log.info("Testing with {}", url);
        SchemaFactory factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema xsdSchema = factory.newSchema(url);
        Validator xsdValidator = xsdSchema.newValidator();

        ProgramUpdate update = ProgramUpdate.create(AbstractApiMediaBackendTest.getBackendVersionNumber(),
            MediaTestDataBuilder.program()
                .withEverything()
                .build());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(update, out);
        log.info(new String(out.toByteArray()));

        Source streamSource = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        xsdValidator.validate(streamSource);


    }

    @Test
    public void testSchema() throws IOException, SAXException {
        assumeThat(AbstractApiMediaBackendTest.getBackendVersionNumber()).isGreaterThanOrEqualTo(AbstractApiTest.DOMAIN_VERSION);

        String baseUrl = CONFIG.requiredOption(Config.Prefix.poms, "baseUrl");
        URL url = new URL(baseUrl + "/schema/vproMedia.xsd");
        log.info("Testing with {}", url);
        SchemaFactory factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI);

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
