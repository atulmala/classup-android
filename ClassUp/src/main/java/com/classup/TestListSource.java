package com.classup;

/**
 * Created by root on 9/20/15.
 */
public class TestListSource {
    private String date;
    private String the_class;
    private String section;
    private String subject;
    private String max_marks;
    private String id;

    public String getId() {
        return id;
    }

    public TestListSource(String date, String the_class, String section, String subject, String max_marks, String id) {
        this.date = date;
        this.the_class = the_class;
        this.section = section;
        this.subject = subject;
        this.max_marks = max_marks;
        this.id = id;

    }

    public String getDate() {
        return date;
    }

    public String getThe_class() {
        return the_class;
    }

    public String getSection() {
        return section;
    }

    public String getSubject() {
        return subject;
    }

    public String getMax_marks() {
        return max_marks;
    }


}
