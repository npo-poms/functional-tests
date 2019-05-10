package nl.vpro.poms.selenium.util.types;

public enum AvType {
    AUDIO("Audio"), VIDEO("Video"), AFWISSELEND("Afwisselend");

    private String type;

    AvType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
