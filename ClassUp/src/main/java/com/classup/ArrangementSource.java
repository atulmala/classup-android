package com.classup;

/**
 * Created by atulgupta on 22/11/17.
 */

public class ArrangementSource {
    private String id;
    private String the_class;
    private String section;
    private String period;

    public ArrangementSource(String id, String the_class, String section, String period) {
        this.id = id;
        this.the_class = the_class;
        this.section = section;
        this.period = period;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
