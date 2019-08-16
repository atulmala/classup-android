package com.classup;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;

/**
 * Created by root on 9/15/15.
 */
public class SessionManager {
    private static SessionManager ourInstance = new SessionManager();
    public static SessionManager getInstance() {
        return ourInstance;
    }

    private SessionManager() {
    }

    static MobileAnalyticsManager analytics;    // 29/06/2017 starting to collect analytics
    String logged_in_user = "";
    public void setLogged_in_user(String user)  {
        logged_in_user = user;
    }
    public String getLogged_in_user()   {
        return logged_in_user;
    }

    String school_id = "";
    public String getSchool_id() {
        return school_id;
    }
    public void setSchool_id(String school_id) {
        this.school_id = school_id;
    }

    Boolean bus_attendance_known = false;
    public Boolean getBus_attendance_known() {
        return bus_attendance_known;
    }
    public void setBus_attendance_known(Boolean bus_attendance_known) {
        this.bus_attendance_known = bus_attendance_known;
    }

    String bus_attendance = "false";
    public String getBus_attendance() {
        return bus_attendance;
    }
    public void setBus_attendance(String bus_attendance) {
        this.bus_attendance = bus_attendance;
    }

    private String image = "";
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public void logout()    {
        school_id = "";
        logged_in_user = "";
    }

    public Boolean whetherLoggedIn()    {
        return logged_in_user != "";
    }
}