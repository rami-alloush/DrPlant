package net.test.drplant;

class Disease {
    private String nameAR;
    private String nameEN;
    private String symptoms;
    private String causes;
    private String precautions;
    private String remedy;

    public Disease() {
    } // Needed for Firebase

    public Disease(String nameAR, String nameEN, String symptoms, String causes, String precautions, String remedy) {
        this.nameAR = nameAR;
        this.nameEN = nameEN;
        this.symptoms = symptoms;
        this.causes = causes;
        this.precautions = precautions;
        this.remedy = remedy;
    }

    public String getNameAR() {
        return nameAR;
    }

    public String getNameEN() {
        return nameEN;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getCauses() {
        return causes;
    }

    public String getPrecautions() {
        return precautions;
    }

    public String getRemedy() {
        return remedy;
    }

    public void setNameAR(String nameAR) {
        this.nameAR = nameAR;
    }

    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public void setCauses(String causes) {
        this.causes = causes;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }

    public void setRemedy(String remedy) {
        this.remedy = remedy;
    }
}
