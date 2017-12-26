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
    private String test_topics;
    private String test_type;
    private String whether_higher_class;

    public String getId() {
        return id;
    }

    public TestListSource(String date, String the_class, String section, String subject,
                          String max_marks, String id) {
        this.date = date;
        this.the_class = the_class;
        this.section = section;
        this.subject = subject;
        this.max_marks = max_marks;
        this.id = id;

    }

    public TestListSource(String date, String the_class, String section, String subject,
                          String max_marks, String id, String test_topics) {
        this.date = date;
        this.the_class = the_class;
        this.section = section;
        this.subject = subject;
        this.max_marks = max_marks;
        this.id = id;
        this.test_topics = test_topics;
    }

    public TestListSource(String date, String the_class, String section, String subject,
                          String max_marks, String id, String test_topics, String test_type) {
        this.date = date;
        this.the_class = the_class;
        this.section = section;
        this.subject = subject;
        this.max_marks = max_marks;
        this.id = id;
        this.test_topics = test_topics;
        this.test_type = test_type;
    }

    public TestListSource(String date, String the_class, String section, String subject,
                          String max_marks, String id, String test_topics,
                          String test_type, String whether_higher_class) {
        this.date = date;
        this.the_class = the_class;
        this.section = section;
        this.subject = subject;
        this.max_marks = max_marks;
        this.id = id;
        this.test_topics = test_topics;
        this.test_type = test_type;
        this.whether_higher_class = whether_higher_class;
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

    public String getTest_topics() {
        return test_topics;
    }

    public String getTest_type() {  return test_type;   }

    public String getWhether_higher_class() {
        return whether_higher_class;
    }
}
