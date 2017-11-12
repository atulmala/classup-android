package com.classup;

/**
 * Created by atulgupta on 28/09/17.
 */

public class CoScholasticSource {
    private String id;
    private String term;
    private String roll_no;
    private String full_name;
    private String parent;
    private String grade_work_ed;
    private String grade_art_ed;
    private String grade_health;
    private String grade_dscpln;
    private String remarks_class_teacher;
    private String promoted_to_class;

    public CoScholasticSource(String id, String term, String roll_no, String full_name,
                              String parent, String grade_work_ed, String grade_art_ed,
                              String grade_health, String grade_dscpln,
                              String remarks_class_teacher, String promoted_to_class) {
        this.id = id;
        this.term = term;
        this.roll_no = roll_no;
        this.full_name = full_name;
        this.parent = parent;
        this.grade_work_ed = grade_work_ed;
        this.grade_art_ed = grade_art_ed;
        this.grade_health = grade_health;
        this.grade_dscpln = grade_dscpln;
        this.remarks_class_teacher = remarks_class_teacher;
        this.promoted_to_class = promoted_to_class;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getGrade_work_ed() {
        return grade_work_ed;
    }

    public void setGrade_work_ed(String grade_work_ed) {
        this.grade_work_ed = grade_work_ed;
    }

    public String getGrade_art_ed() {
        return grade_art_ed;
    }

    public void setGrade_art_ed(String grade_art_ed) {
        this.grade_art_ed = grade_art_ed;
    }

    public String getGrade_health() {
        return grade_health;
    }

    public void setGrade_health(String grade_health) {
        this.grade_health = grade_health;
    }

    public String getGrade_dscpln() {
        return grade_dscpln;
    }

    public void setGrade_dscpln(String grade_dscpln) {
        this.grade_dscpln = grade_dscpln;
    }

    public String getRemarks_class_teacher() {
        return remarks_class_teacher;
    }

    public void setRemarks_class_teacher(String remarks_class_teacher) {
        this.remarks_class_teacher = remarks_class_teacher;
    }

    public String getPromoted_to_class() {
        return promoted_to_class;
    }

    public void setPromoted_to_class(String promoted_to_class) {
        this.promoted_to_class = promoted_to_class;
    }
}
