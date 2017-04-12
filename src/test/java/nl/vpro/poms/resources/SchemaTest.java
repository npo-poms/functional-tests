package nl.vpro.poms.resources;

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
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.Config;

/**
 * @author Michiel Meeuwissen
 */
public class SchemaTest {


    @Test
    public void testSchema() throws IOException, SAXException {
        String baseUrl = Config.requiredOption(Config.Prefix.poms, "baseUrl");
        SchemaFactory factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema xsdSchema = factory.newSchema(
            new URL(baseUrl + "/schema/update/vproMediaUpdate.xsd"));
        Validator xsdValidator = xsdSchema.newValidator();

        ProgramUpdate update = ProgramUpdate.create(MediaTestDataBuilder.program()
            .withEverything());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(update, out);
        System.out.println(new String(out.toByteArray()));

        Source streamSource = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        xsdValidator.validate(streamSource);




    }
}
