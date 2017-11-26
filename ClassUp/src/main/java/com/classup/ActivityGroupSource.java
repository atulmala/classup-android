package com.classup;

/**
 * Created by atulgupta on 25/11/17.
 */

public class ActivityGroupSource {
    private String id;
    private String activity_group;
    private String incharge;
    private String incharge_email;

    public ActivityGroupSource(String id, String activity_group, String incharge,
                               String incharge_email) {
        this.id = id;
        this.activity_group = activity_group;
        this.incharge = incharge;
        this.incharge_email = incharge_email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActivity_group() {
        return activity_group;
    }


    public String getIncharge() {
        return incharge;
    }


    public String getIncharge_email() {
        return incharge_email;
    }
}
