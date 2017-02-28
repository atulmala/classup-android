package com.classup;

/**
 * Created by atulgupta on 28/02/17.
 */

public class TeacherListSource {

    public String getId() {
        return id;
    }

    public String getFull_name() {
        String full_name = first_name + " " + last_name;
        return full_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getLogin_id() {
        return login_id;
    }



    public TeacherListSource(String id, String first_name, String last_name,
                             String mobile, String login_id) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;

        this.mobile = mobile;
        this.login_id = login_id;
    }

    private String id;
    private String first_name;
    private String last_name;
    private String mobile;
    private String login_id;
}
