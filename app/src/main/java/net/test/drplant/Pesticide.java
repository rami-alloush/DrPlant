package net.test.drplant;

import java.util.List;

class Pesticide {
    private String nameAR;
    private String nameEN;
    private String details;
    private String usage;
    private String warnings;
    private List<String> usedwith;

    public Pesticide() {
    } // Needed for Firebase

    public Pesticide(String nameAR, String nameEN, String details, String usage, String warnings, List<String> usedwith) {
        this.nameAR = nameAR;
        this.nameEN = nameEN;
        this.details = details;
        this.usage = usage;
        this.warnings = warnings;
        this.usedwith = usedwith;
    }

    public String getNameAR() {
        return nameAR;
    }

    public void setNameAR(String nameAR) {
        this.nameAR = nameAR;
    }

    public String getNameEN() {
        return nameEN;
    }

    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
    }

    public String getdetails() {
        return details;
    }

    public String getusage() {
        return usage;
    }

    public String getwarnings() {
        return warnings;
    }

    public List<String> getusedwith() {
        return usedwith;
    }

    public void setdetails(String details) {
        this.details = details;
    }

    public void setusage(String usage) {
        this.usage = usage;
    }

    public void setwarnings(String warnings) {
        this.warnings = warnings;
    }

    public void setusedwith(List<String> usedwith) {
        this.usedwith = usedwith;
    }
}
