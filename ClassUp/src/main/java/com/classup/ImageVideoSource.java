package com.classup;

public class ImageVideoSource {
    private String id;
    private String date;
    private String type;
    private String teacher;
    private String the_class;
    private String section;
    private String description;
    private String location;
    private String short_link;

    public ImageVideoSource(String id, String date, String type, String teacher, String the_class,
                            String section, String description, String location, String short_link)
    {
        this.id = id;
        this.date = date;
        this.type = type;
        this.teacher = teacher;
        this.the_class = the_class;
        this.section = section;
        this.description = description;
        this.location = location;
        this.short_link = short_link;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getThe_class() {
        return the_class;
    }

    public String getLocation() {
        return location;
    }

    public String getSection() {
        return section;
    }

    public String getShort_link() {
        return short_link;
    }

    public String getDescription() {
        return description;
    }
}
