package nl.vpro.poms.helpers;

import java.util.List;

public class SegmentXmlBuilder {

    private String avType;
    private String midRef;
    private String crid;
    private String broadcaster;
    private String titleType;
    private String title;
    private List<String> images;
    private String start;
    private boolean addNS = false;

    private static final String BASE_XML =
            "<segment avType=\"%s\"%s%s>\n" +
            "    <crid>%s</crid>\n" +
            "    <broadcaster>%s</broadcaster>\n" +
            "    <title type=\"%s\">%s</title>\n" +
            "    <images>\n" +
            "        %s\n" +
            "    </images>\n" +
            "    <start>%s</start>\n" +
            "</segment>";
    private static final String MIDREF_TPL = " midRef=\"%s\"";
    private static final String NS = " xmlns=\"urn:vpro:media:update:2009\"";

    public SegmentXmlBuilder midRef(String midRef) {
        this.midRef = midRef;
        return this;
    }

    public SegmentXmlBuilder avType(String avType) {
        this.avType = avType;
        return this;
    }

    public SegmentXmlBuilder crid(String crid) {
        this.crid = crid;
        return this;
    }

    public SegmentXmlBuilder broadcaster(String broadcaster) {
        this.broadcaster = broadcaster;
        return this;
    }

    public SegmentXmlBuilder titleType(String titleType) {
        this.titleType = titleType;
        return this;
    }

    public SegmentXmlBuilder title(String title) {
        this.title = title;
        return this;
    }

    public SegmentXmlBuilder images(List<String> images) {
        this.images = images;
        return this;
    }

    public SegmentXmlBuilder start(String start) {
        this.start = start;
        return this;
    }

    public SegmentXmlBuilder addNS(boolean addNS) {
        this.addNS = addNS;
        return this;
    }

    public String build() {
        String imagesXML = String.join("\n", images);
        String midRefAttr = (midRef != null ? String.format(MIDREF_TPL, midRef) : "");
        String nsAttr = (addNS ? NS : "");
        return String.format(BASE_XML, avType, midRefAttr, nsAttr, crid, broadcaster, titleType, title, imagesXML, start);
    }
}
