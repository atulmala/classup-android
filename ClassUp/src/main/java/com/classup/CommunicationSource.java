package com.classup;

/**
 * Created by atulgupta on 15/10/16.
 */

public class CommunicationSource {
    private String id;
    private String date;
    private String text;

    public String getId() {
        return id;
    }

    public CommunicationSource(String id, String date, String text) {
        this.id = id;
        this.date = date;
        this.text = text;
    }

    public void setId(String id) {
        this.id = id;

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
