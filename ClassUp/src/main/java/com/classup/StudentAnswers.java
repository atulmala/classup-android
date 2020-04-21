package com.classup;

public class StudentAnswers {
    private String student_id;
    private String question_id;
    private String option_marked = "X";

    public StudentAnswers(String student_id, String question_id, String option_marked) {
        this.student_id = student_id;
        this.question_id = question_id;
        this.option_marked = option_marked;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getOption_marked() {
        return option_marked;
    }

    public void setOption_marked(String option_marked) {
        this.option_marked = option_marked;
    }
}
