package nl.vpro.poms.helpers;

import java.util.List;

public class ProgramXmlBuilder {

    private String avType;
    private String type;
    private String crid;
    private String broadcaster;
    private String titleType;
    private String title;
    private List<String> segments;
    private List<String> images;

    private static final String BASE_XML =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<program avType=\"%s\" type=\"%s\"\n" +
            "    xmlns=\"urn:vpro:media:update:2009\">\n" +
            "    <crid>%s</crid>\n" +
            "    <broadcaster>%s</broadcaster>\n" +
            "    <title type=\"%s\">%s</title>\n" +
            "    <images>\n" +
            "        %s\n" +
            "    </images>\n" +
            "    <segments>\n" +
            "        %s\n" +
            "    </segments>\n" +
            "</program>";

    public ProgramXmlBuilder avType(String avType) {
        this.avType = avType;
        return this;
    }

    public ProgramXmlBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ProgramXmlBuilder crid(String crid) {
        this.crid = crid;
        return this;
    }

    public ProgramXmlBuilder broadcaster(String broadcaster) {
        this.broadcaster = broadcaster;
        return this;
    }

    public ProgramXmlBuilder titleType(String titleType) {
        this.titleType = titleType;
        return this;
    }

    public ProgramXmlBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ProgramXmlBuilder segments(List<String> segments) {
        this.segments = segments;
        return this;
    }

    public ProgramXmlBuilder images(List<String> images) {
        this.images = images;
        return this;
    }

    public String build() {
        String segmentsXML = String.join("\n", segments);
        String imagesXML = String.join("\n", images);
        return String.format(BASE_XML, avType, type, crid, broadcaster, titleType, title, imagesXML, segmentsXML);
    }
}
