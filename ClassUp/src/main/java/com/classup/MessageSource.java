package com.classup;

/**
 * Created by atulgupta on 29/11/17.
 */

public class MessageSource {
    private String id;
    private String date;
    private String message;
    private String sent_to;
    private String the_class;
    private String section;
    private String activity_group;
    private String teacher;

    public MessageSource(String id, String date, String message,
                         String sent_to, String the_class, String section,
                         String activity_group, String teacher) {
        this.id = id;
        this.date = date;
        this.message = message;
        this.sent_to = sent_to;
        this.the_class = the_class;
        this.section = section;
        this.activity_group = activity_group;
        this.teacher = teacher;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSent_to() {
        return sent_to;
    }

    public void setSent_to(String sent_to) {
        this.sent_to = sent_to;
    }

    public String getThe_class() {
        return the_class;
    }

    public void setThe_class(String the_class) {
        this.the_class = the_class;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getActivity_group() {
        return activity_group;
    }

    public void setActivity_group(String activity_group) {
        this.activity_group = activity_group;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }
}
