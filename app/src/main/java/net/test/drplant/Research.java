package net.test.drplant;

import android.util.Log;

import com.google.firebase.Timestamp;

class Research {
    public String nameAR;
    public String corp;
    public String content;
    public Timestamp timestamp;
    public Timestamp date;

    public Research() {
    } // Needed for Firebase

    public Research(String nameAR, String corp,  String content, Timestamp timestamp ) {
        this.nameAR = nameAR;
        this.content = content;
        this.corp = corp;
        this.timestamp = timestamp;
    }

    public String getNameAR() {
        return nameAR;
    }

    public void setNameAR(String nameAR) {
        this.nameAR = nameAR;
    }

    String getcorp() {
        return corp;
    }

    public void setcorp(String corp) {
        this.corp = corp;
    }

    public String getContent() {
        return content;
    }

    public void setcontent(String content) {
        this.content = content;
    }

    Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
