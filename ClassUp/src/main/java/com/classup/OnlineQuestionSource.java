package com.classup;

public class OnlineQuestionSource {
    private String id;
    private Integer q_no;
    private String question;
    private String option_A;
    private String option_B;
    private String option_C;
    private String option_D;

    public OnlineQuestionSource(String id, int q_no, String question, String option_A,
                                String option_B, String option_C, String option_D) {
        this.id = id;
        this.q_no = q_no;
        this.question = question;
        this.option_A = option_A;
        this.option_B = option_B;
        this.option_C = option_C;
        this.option_D = option_D;
    }

    public String getId() {
        return id;
    }

    public String getQ_no() {
        return q_no.toString();
    }

    public String getQuestion() {
        return question;
    }

    public String getOption_A() {
        return option_A;
    }

    public String getOption_B() {
        return option_B;
    }

    public String getOption_C() {
        return option_C;
    }

    public String getOption_D() {
        return option_D;
    }
}
