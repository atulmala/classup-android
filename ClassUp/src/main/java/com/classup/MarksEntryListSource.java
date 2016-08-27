package com.classup;

/**
 * Created by atulgupta on 02/10/15.
 */
public class MarksEntryListSource {
    private String id;
    private String roll_no;
    private String full_name;
    private String marks;
    private String grade;

    public MarksEntryListSource(String id, String roll_no, String name, String marks, String grade) {
        this.marks = marks;
        this.id = id;
        this.grade = grade;
        this.full_name = name;
        this.roll_no = roll_no;
    }

    public String getId() {
        return id;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getMarks() {
        return marks;
    }

    public String getGrade() {
        return grade;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
