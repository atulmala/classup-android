package com.classup;

/**
 * Created by atulgupta on 02/10/15.
 */
public class MarksEntryListSource {
    private String id;
    private String roll_no;
    private String full_name;
    private String parent;
    private String marks;
    private String grade;

    // 23/09/2017 for Term test
    private String periodic_test_marks;
    private String notebook_submission_marks;
    private String subject_enrichment_marks;

    public MarksEntryListSource(String id, String roll_no, String full_name, String parent,
                                String marks, String grade, String periodic_test_marks,
                                String notebook_submission_marks, String subject_enrichment_marks) {
        this.id = id;
        this.roll_no = roll_no;
        this.full_name = full_name;
        this.parent = parent;
        this.marks = marks;
        this.grade = grade;
        this.periodic_test_marks = periodic_test_marks;
        this.notebook_submission_marks = notebook_submission_marks;
        this.subject_enrichment_marks = subject_enrichment_marks;
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

    public String getParent() {
        return parent;
    }

    public void setPeriodic_test_marks(String periodic_test_marks) {
        this.periodic_test_marks = periodic_test_marks;
    }

    public void setNotebook_submission_marks(String notebook_submission_marks) {
        this.notebook_submission_marks = notebook_submission_marks;
    }

    public void setSubject_enrichment_marks(String subject_enrichment_marks) {
        this.subject_enrichment_marks = subject_enrichment_marks;
    }

    public String getPeriodic_test_marks() {
        return periodic_test_marks;
    }

    public String getNotebook_submission_marks() {
        return notebook_submission_marks;
    }

    public String getSubject_enrichment_marks() {
        return subject_enrichment_marks;
    }
}
