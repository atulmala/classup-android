package com.classup;

/**
 * Created by atulgupta on 08/04/18.
 */

public class TTSource {
    private String the_class;
    private String period;
    private String section;
    private String subject;

    public TTSource(String the_class, String period, String section, String subject) {
        this.the_class = the_class;
        this.period = period;
        this.section = section;
        this.subject = subject;
    }

    public String getThe_class() {
        return the_class;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
