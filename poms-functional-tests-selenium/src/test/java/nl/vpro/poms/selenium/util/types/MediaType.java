package nl.vpro.poms.selenium.util.types;

public enum MediaType {
    CLIP("Clip"),
    TRAILER("Trailer"),
    UITZENDING("Uitzending");

    private String type;

    MediaType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
