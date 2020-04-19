package com.classup;

public class OnlineTestSource {
    private String id;
    private String date;
    private String subject;
    private String the_class;
    private int duration;

    public OnlineTestSource(String id, String date, String subject, String the_class, int duration)
    {
        this.id = id;
        this.date = date;
        this.subject = subject;
        this.the_class = the_class;
        this.duration = duration;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setThe_class(String the_class) {
        this.the_class = the_class;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getThe_class() {
        return the_class;
    }

    public int getDuration() {
        return duration;
    }
}
