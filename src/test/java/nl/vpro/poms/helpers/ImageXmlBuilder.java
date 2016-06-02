package nl.vpro.poms.helpers;

public class ImageXmlBuilder {

    private String type;
    private String title;
    private String imageLocationUrl;

    private static final String BASE_XML =
            "<image type=\"%s\">\n" +
            "    <title>%s</title>\n" +
            "    <imageLocation>\n" +
            "        <url>%s</url>\n" +
            "    </imageLocation>\n" +
            "</image>";

    public ImageXmlBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ImageXmlBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ImageXmlBuilder imageLocationUrl(String imageLocationUrl) {
        this.imageLocationUrl = imageLocationUrl;
        return this;
    }

    public String build() {
        return String.format(BASE_XML, type, title, imageLocationUrl);
    }
}
