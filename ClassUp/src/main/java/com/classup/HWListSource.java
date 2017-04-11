package com.classup;

/**
 * Created by atulgupta on 10/04/17.
 */

public class HWListSource {
    private String id;
    private String teacher;
    private String the_class;
    private String section;
    private String subject;
    private String due_date;
    private String notes;

    public HWListSource(String id, String teacher, String the_class, String section,
                        String subject, String due_date, String notes) {
        this.id = id;
        this.teacher = teacher;
        this.the_class = the_class;
        this.section = section;
        this.subject = subject;
        this.due_date = due_date;
        this.notes = notes;
    }

    public String getThe_class() {
        return the_class + '-' + section;
    }

    public String getSubject() {
        return subject;
    }

    public String getDue_date() {
        return due_date;
    }
}
