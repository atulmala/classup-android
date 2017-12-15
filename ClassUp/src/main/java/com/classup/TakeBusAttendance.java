package com.classup;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TakeBusAttendance extends AppCompatActivity {
    BusAttendanceAdapter ptr_adapter;
    String tag = "TakeBusAttendance";



    final ArrayList<AttendanceListSource> student_list = new ArrayList<>();
    private ArrayList<String> stop_list = new ArrayList<>();
    final ArrayList<String> current_absent_students = new ArrayList<>();
    final ArrayList<String> already_absent_students = new ArrayList<>();
    final ArrayList<String> correction_list = new ArrayList<>();

    Context context;
    final Activity activity = this;

    String server_ip = "";
    String teacher = "";
    String rout_type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_bus_attendance);

        try {
            AnalyticsEvent event =
                    SessionManager.getInstance().analytics.getEventClient().
                            createEvent("Bus Attendance");
            event.addAttribute("user", SessionManager.getInstance().
                    getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Bus Attendance");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Bus Attendance");
        }

        context = this;
        this.setTitle("Bus Attendance");

        server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        final Intent intent = getIntent();

        final BusAttendanceAdapter adapter = new BusAttendanceAdapter(this,
                android.R.layout.simple_list_item_checked, student_list,
                already_absent_students, intent);
        ptr_adapter = adapter;
        final ListView listView = (ListView) findViewById(R.id.student_list_bus);
        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(1);
        listView.setAdapter(adapter);

        final String stop_list_url =  server_ip + "/bus_attendance/retrieve_bus_stops/" +
                school_id + "/" + intent.getStringExtra("rout") + "/?format=json";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest1 = new JsonArrayRequest
                (Request.Method.GET, stop_list_url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.hide();
                        progressDialog.dismiss();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);

                                final String stop_name = jo.getString("stop_name");
                                stop_list.add(stop_name);

                                // now get the list of students who board the bus on this rout from
                                // this stop

                                final String student_list_url = ( server_ip +
                                        "/bus_attendance/list_rout_students1/" + school_id + "/" +
                                        intent.getStringExtra("rout") + "/"
                                        + stop_name + "/?format=json").replace(" ", "%20");
                                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                                        Request.Method.GET, student_list_url, null,
                                        new Response.Listener<JSONArray>() {
                                            public void onResponse(JSONArray response) {
                                                String blank = "";
                                                student_list.add(new AttendanceListSource(blank,
                                                        blank, blank, stop_name, "bus_stop"));
                                                progressDialog.hide();
                                                progressDialog.dismiss();
                                                for (int j = 0; j < response.length(); j++) {
                                                    try {
                                                        JSONObject jo =
                                                                response.getJSONObject(j);
                                                        String f_name = jo.getString("fist_name");
                                                        String l_name = jo.getString("last_name");
                                                        String the_class =
                                                                jo.getString("current_class");
                                                        String the_section =
                                                                jo.getString("current_section");
                                                        Integer serial = j + 1;
                                                        String full_name = serial.toString() + ".  "
                                                                + f_name + " " +
                                                                l_name + " (" + the_class
                                                                + "-" + the_section + ")";

                                                        // get the id of the student
                                                        String id = jo.getString("id");
                                                        if (!ptr_adapter.taken_earlier.get(0).
                                                                equals("true"))
                                                            current_absent_students.add(id);

                                                        // as this is bus
                                                        // attendance we do not need the roll
                                                        // number. But this code has
                                                        // been copied from SelectStudent let's
                                                        // assign blank string
                                                        String roll_no = "";
                                                        // put all the above details
                                                        // into the adapter.
                                                        student_list.add
                                                                (new AttendanceListSource
                                                                        (roll_no, full_name, id,
                                                                                stop_name,
                                                                                "student_name"));
                                                        //adapter.notifyDataSetChanged();
                                                    } catch (JSONException je) {
                                                        System.out.println
                                                                ("Ran into JSON exception " +
                                                                        "while trying to fetch the "
                                                                        + "list of students");
                                                        je.printStackTrace();
                                                    } catch (Exception e) {
                                                        System.out.println("Caught General " +
                                                                "exception " +
                                                                "while trying to fetch the " +
                                                                "list of students");
                                                        e.printStackTrace();
                                                    }
                                                }
                                                System.out.println("list at this stage:");
                                                for(int i=0; i<student_list.size(); i++)
                                                    student_list.get(i).show();
                                                progressDialog.hide();
                                                progressDialog.dismiss();
                                                adapter.notifyDataSetChanged();
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println
                                                ("inside volley error handler");
                                        progressDialog.hide();
                                        progressDialog.dismiss();
                                        if (error instanceof TimeoutError ||
                                                error instanceof NoConnectionError) {
                                            if (!MiscFunctions.getInstance().
                                                    checkConnection
                                                            (getApplicationContext())) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Slow network connection or" +
                                                                " No internet " +
                                                                "connectivity",
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Slow network connection or" +
                                                                " No internet connectivity",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        } else if (error instanceof ServerError) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Slow network connection or " +
                                                            "No internet connectivity",
                                                    Toast.LENGTH_LONG).show();
                                        } else if (error instanceof NetworkError) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Slow network connection or" +
                                                            " No internet connectivity",
                                                    Toast.LENGTH_LONG).show();
                                        } else if (error instanceof ParseError) {
                                            //TODO
                                        }
                                        // TODO Auto-generated method stub
                                    }
                                });
                                AppController.getInstance().
                                        addToRequestQueue(jsonArrayRequest, tag);
                            }
                            catch (JSONException je) {
                                System.out.println
                                        ("Ran into JSON exception while trying to fetch the "
                                                + "list of students");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the list of students");
                                e.printStackTrace();
                            }
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            if (!MiscFunctions.getInstance().checkConnection
                                    (getApplicationContext())) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest1, tag);

        // retrieve the list of already absent students, if the attendance has been earlier taken
        // for the same rout, type (to school or from school) and date
        already_absent_students.clear();
        String url =  server_ip + "/bus_attendance/retrieve_bus_attendance/" + school_id + "/" +
                intent.getStringExtra("rout") + "/" + intent.getStringExtra("rout_type") +
                "/" + intent.getStringExtra("date") + "/" + intent.getStringExtra("month") + "/"
                + intent.getStringExtra("year") + "/?format=json";
        url = url.replace(" ", "%20");

        progressDialog.show();

        JsonArrayRequest jsonArrayRequest2 = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.hide();
                        progressDialog.dismiss();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                // get the id of of the absent student and
                                // add it to absentee list
                                already_absent_students.add(jo.getString("student"));
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception " +
                                        "while trying to fetch the list of absentees");
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while trying to fetch the list of absentees");
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        progressDialog.hide();
                        progressDialog.dismiss();
                        if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                            if(!MiscFunctions.getInstance().checkConnection
                                    (getApplicationContext())) {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            } else  {
                                Toast.makeText(getApplicationContext(),
                                        "Slow network connection or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            }
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Slow network connection or No internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest2, tag);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.first_time = false;
                CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.lbl_roll_no);

                if(textView != null)
                    if (!textView.isChecked()) {
                        // the student is being marked as present.
                        textView.setChecked(true);
                        textView.setBackgroundColor(Color.WHITE);

                        if (current_absent_students.contains(student_list.get(i).getId())) {
                            current_absent_students.remove(student_list.get(i).getId());
                        }
                        if (!correction_list.contains(student_list.get(i).getId()))
                            correction_list.add(student_list.get(i).getId());

                        adapter.marked_students.add(student_list.get(i).getId());
                        adapter.notifyDataSetChanged();

                        // if this student was marked as absent in an earlier attendance, he/she is
                        // now marked as present. Hence, the database should be updated
                        if (already_absent_students.contains(student_list.get(i).getId())) {
                            already_absent_students.remove(student_list.get(i).getId());
                            adapter.already_absent_students.remove((student_list.get(i).getId()));
                            adapter.notifyDataSetChanged();
                        }

                        // also add to the selected subjects list of the adapter
                        //adapter.already_absent_students.add(student_list.get(i).getId());
                        adapter.notifyDataSetChanged();
                    } else {
                        // the student is being turned absent from present
                        textView.setChecked(false);
                        //textView.setBackgroundColor(Color.YELLOW);

                        if (!current_absent_students.contains(student_list.get(i).getId()))
                            current_absent_students.add(student_list.get(i).getId());

                        // if this student was absent in earlier attendance, this needs to be removed
                        // from the list of already absent students, else absent count will be flawed
                        if (already_absent_students.contains(student_list.get(i).getId()))
                            already_absent_students.remove(student_list.get(i).getId());

                        if (correction_list.contains(student_list.get(i).getId()))
                            correction_list.remove(student_list.get(i).getId());
                        // also add to the absent students list of the adapter
                        adapter.already_absent_students.add(student_list.get(i).getId());
                        adapter.marked_students.remove(student_list.get(i).getId());
                        adapter.notifyDataSetChanged();
                    }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int ii = i;
                final String student_name = student_list.get(i).getFull_name();
                android.app.AlertDialog.Builder builder =
                        new android.app.AlertDialog.Builder(activity);
                builder.setMessage("Do you want to call the parent of  " + student_name + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String student_id = student_list.get(ii).getId();
                                String server_ip = MiscFunctions.getInstance().
                                        getServerIP(activity);
                                String url = server_ip + "/student/get_parent/" + student_id + "/";
                                url = url.replace(" ", "%20");
                                progressDialog.show();
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                        (Request.Method.GET, url, null,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        try {
                                                            String p_m1 = response.get
                                                                    ("parent_mobile1").
                                                                    toString();
                                                            System.out.println("mobile=" + p_m1);
                                                            Intent intent = new Intent
                                                                    (Intent.ACTION_CALL);
                                                            intent.setData(Uri.parse
                                                                    ("tel:" + p_m1));
                                                            System.out.println
                                                                    ("going to make call");
                                                            // check to see if dialler permssion exist
                                                            int permissionCheck =
                                                                    ContextCompat.checkSelfPermission
                                                                            (activity,
                                                                                    android.Manifest
                                                                                            .permission.CALL_PHONE);
                                                            if (permissionCheck ==
                                                                    PackageManager.
                                                                            PERMISSION_GRANTED)
                                                                startActivity(intent);
                                                            else
                                                                Toast.makeText(
                                                                        getApplicationContext(),
                                                                        "Dialling permission " +
                                                                                "not granted",
                                                                        Toast.LENGTH_LONG).show();

                                                        } catch (JSONException je) {
                                                            System.out.println("Ran into " +
                                                                    "JSON exception " +
                                                                    "while trying to make call");
                                                            je.printStackTrace();
                                                        } catch (Exception e) {
                                                            System.out.println("Caught " +
                                                                    "General exception " +
                                                                    "while trying make call ");
                                                            e.printStackTrace();
                                                        }


                                                        progressDialog.hide();
                                                        progressDialog.dismiss();

                                                    }
                                                }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                progressDialog.hide();
                                                progressDialog.dismiss();
                                                if (error instanceof TimeoutError ||
                                                        error instanceof NoConnectionError) {
                                                    if (!MiscFunctions.getInstance().checkConnection
                                                            (getApplicationContext())) {
                                                        Toast.makeText(getApplicationContext(),
                                                                "Slow network connection or " +
                                                                        "No internet connectivity",
                                                                Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(),
                                                                "Slow network connection or" +
                                                                        " No internet connectivity",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                } else if (error instanceof ServerError) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Slow network connection or" +
                                                                    " No internet connectivity",
                                                            Toast.LENGTH_LONG).show();
                                                } else if (error instanceof NetworkError) {

                                                } else if (error instanceof ParseError) {

                                                    Toast.makeText(getApplicationContext(),
                                                            "Error in parsing of number",
                                                            Toast.LENGTH_LONG).show();
                                                    System.out.println(error);
                                                }
                                            }
                                        });
                                // here we can sort the attendance list as per roll number

                                com.classup.AppController.getInstance().
                                        addToRequestQueue(jsonObjectRequest, tag);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();
                return true;
            }
        });

        teacher = SessionManager.getInstance().getLogged_in_user();
        rout_type = intent.getStringExtra("rout_type");


    }
    private void clearAbsentee_list(BusAttendanceAdapter  adapter) {
        adapter.clearAbsentee_list();
        //current_absent_students.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Submit").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 1, 0, "Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                submitBusAttendance(getIntent());
                break;
            case 1:
                startActivity(new Intent("com.classup.TeacherMenu").
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    void submitBusAttendance(final Intent intent)  {

        final String d = intent.getStringExtra("date");
        final String m = intent.getStringExtra("month");
        final String y = intent.getStringExtra("year");
        final String school_id = SessionManager.getInstance().getSchool_id();
        // create the dialog box for confirmation
        final Dialog dialog = new Dialog(TakeBusAttendance.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bus_attendance_submission);

        // show the date of attendance
        TextView txt_date =
                (TextView) dialog.findViewById(R.id.txt_bus_att_submission_date);

        String formatted_date = d + "/" + m + "/" + y;
        txt_date.setText(formatted_date);
        txt_date.setTypeface(Typeface.DEFAULT_BOLD);

        // show the rout
        TextView txt_rout =
                (TextView) dialog.findViewById((R.id.txt_bus_att_submission_rout));
        final String rout =
                intent.getStringExtra("rout");
        txt_rout.setText(rout);
        txt_rout.setTypeface(Typeface.DEFAULT_BOLD);

        // show absent count
        Integer absent_count;
        if (ptr_adapter.taken_earlier.get(0).equals("true")) {
            for (String id:current_absent_students)    {
                if (!already_absent_students.contains(id))
                    already_absent_students.add(id);
            }
            //absent_count = current_absent_students.size() + already_absent_students.size();
            absent_count = already_absent_students.size();
        }
        else
            absent_count = current_absent_students.size();
        TextView txt_absent =
                (TextView) dialog.findViewById((R.id.txt_bus_att_absent_count));
        txt_absent.setText(absent_count.toString());
        txt_absent.setTypeface(Typeface.DEFAULT_BOLD);

        // show present count. We need to deduct the number of stops
        Integer present_count = student_list.size() - stop_list.size() - absent_count;
        TextView txt_present_count =
                (TextView) dialog.findViewById(R.id.txt_bus_att_present_count);
        txt_present_count.setText(present_count.toString());
        txt_present_count.setTypeface(Typeface.DEFAULT_BOLD);

        // now, show the dialog
        dialog.show();

        Button btn_ok = (Button) dialog.findViewById(R.id.btn_bus_att_confirm);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
                dialog.dismiss();
                // update the server tables to indicate that the attendance for this
                // bus rout and date was taken
                String url =  server_ip +
                        "/bus_attendance/bus_attendance_taken/" + school_id + "/" + rout + "/" +
                        rout_type + "/" + d + "/" + m + "/" + y + "/" + teacher + "/";
                url = url.replace(" ", "%20");

                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                final Context context = getApplicationContext();
                                String text = "Looks there is a problem with connection" +
                                        " or server end. Please check and try again";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, text, duration);

                                toast.setGravity(Gravity.CENTER_HORIZONTAL |
                                        Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();

                                error.printStackTrace();
                                return;
                            }
                        });
                com.classup.AppController.getInstance().addToRequestQueue(request, tag);

                //call the API for submitting the attendance to backend
                String tag = "Update Attendance";
                JSONObject jsonObject = new JSONObject();
                try {
                    for (String id : current_absent_students)
                        jsonObject.put(id, id);
                    if(rout_type.equals("from_school")) {
                        for(String id : already_absent_students)
                            jsonObject.put(id, id);
                    }
                } catch (JSONException je) {
                    System.out.println("unable to create json for " +
                            "absentees to be deleted");
                    je.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException ae) {
                    System.out.println("array out of bounds exception");
                    ae.printStackTrace();
                }

                String url1 =  server_ip +
                        "/bus_attendance/process_bus_attendance1/" + rout_type + "/" +
                        d + "/" + m + "/" + y + "/" + teacher + "/";
                url1 = url1.replace(" ", "%20");
                JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.POST,
                        url1, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                com.classup.AppController.getInstance().
                        addToRequestQueue(request1, tag);
                //}
                // clear the absentee list
                clearAbsentee_list(ptr_adapter);

                // now process the correction list
                JSONObject jsonObject2 = new JSONObject();
                try {
                    for (String id : correction_list)
                        jsonObject2.put(id, id);
                }catch (JSONException je) {
                    System.out.println("unable to create json for " +
                            "absentees to be deleted");
                    je.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException ae) {
                    System.out.println("array out of bounds exception");
                    ae.printStackTrace();
                }
                String url2 =  server_ip +
                        "/bus_attendance/delete_bus_attendance1/" +
                        intent.getStringExtra("rout_type") +
                        "/" + intent.getStringExtra("date") + "/" +
                        intent.getStringExtra("month") + "/"
                        + intent.getStringExtra("year") + "/?format=json";
                url2 = url2.replace(" ", "%20");
                JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.POST,
                        url2, jsonObject2, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                com.classup.AppController.getInstance().
                        addToRequestQueue(request2, tag);
                // show the toast that attendance has successfully been submitted
                Toast.makeText(getApplicationContext(), "Attendance submitted to server",
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent("com.classup.TeacherMenu"));
            }
        });
        // processing for Cancel button
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_bus_att_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //startActivity(new Intent("com.classup.TeacherMenu"));
            }
        });
    }
}