package com.classup;

public class OnlineClassSource {
    private String id;
    private String date;
    private String subject;
    private String teacher;
    private String the_class;
    private String topic;
    private String pdf_link;
    private String youtube_link;

    public OnlineClassSource(String id, String date, String subject, String teacher,
                             String the_class, String topic,
                             String pdf_link, String youtube_link) {
        this.id = id;
        this.date = date;
        this.subject = subject;
        this.teacher = teacher;
        this.the_class = the_class;
        this.topic = topic;
        this.pdf_link = pdf_link;
        this.youtube_link = youtube_link;
    }

    public String getId() {
        return id;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getThe_class() {
        return the_class;
    }

    public void setThe_class(String the_class) {
        this.the_class = the_class;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPdf_link() {
        return pdf_link;
    }

    public void setPdf_link(String pdf_link) {
        this.pdf_link = pdf_link;
    }

    public String getYoutube_link() {
        return youtube_link;
    }

    public void setYoutube_link(String youtube_link) {
        this.youtube_link = youtube_link;
    }
}
