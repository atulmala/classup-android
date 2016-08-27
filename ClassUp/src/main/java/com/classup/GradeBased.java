package com.classup;

/**
 * Created by root on 9/17/15.
 */
public class GradeBased {
    String grade_based;
    private static GradeBased ourInstance = new GradeBased();

    public static GradeBased getInstance() {
        return ourInstance;
    }

    private GradeBased() {
        grade_based = "False";
    }
    public void setGrade_based(String value)    {
        grade_based = value;
    }
    public String getGrade_based()  {
        return grade_based;
    }
}
