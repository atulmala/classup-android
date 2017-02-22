package com.classup;


/**
 * Created by atulgupta on 07/08/15.
 */
public   class AttendanceListSource  {
    private String full_name;
    private String roll_number;
    private String id;
    private String erp_id;

    // for bus attendance
    private String bus_stop;
    private String entry_type;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public AttendanceListSource(String roll_number, String full_name, String id, String erp_id) {
        super();
        this.full_name = full_name;
        this.roll_number = roll_number;
        this.id = id;
        this.erp_id = erp_id;
    }

    public String getEntry_type() {
        return entry_type;
    }

    public String getBus_stop() {
        return bus_stop;
    }

    public AttendanceListSource(String roll_number, String full_name, String id,
                                String bus_stop, String entry_type) {
        super();
        this.full_name = full_name;
        this.roll_number = roll_number;
        this.id = id;
        this.bus_stop = bus_stop;
        this.entry_type = entry_type;
    }

    public void show()  {
        System.out.println(full_name + "/" + bus_stop + "/" + entry_type);
    }

    public String getFull_name() {
        return full_name;
    }
    public String getName_rollno()  {
        if (roll_number.equals("")) // in case of bus attendance we do not need to show roll number
            return full_name;
        else
            return roll_number + "      " + full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getRoll_number() {
        return roll_number;
    }

    public String getErp_id() {
        return erp_id;
    }

    public void setRoll_number(String roll_number) {
        this.roll_number = roll_number;
    }
}