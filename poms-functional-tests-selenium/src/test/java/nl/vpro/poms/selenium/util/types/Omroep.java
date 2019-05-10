package nl.vpro.poms.selenium.util.types;

public enum Omroep {
    NPS("NPS"), VPRO("VPRO");

    private String omroep;

    Omroep(String omroep) {
        this.omroep = omroep;
    }

    public String getOmroep() {
        return omroep;
    }
}
