package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TeachersAttendance extends AppCompatActivity {
    final Activity activity = this;
    TeacherAttAdapter adapter;
    final ArrayList<String> correction_list = new ArrayList<>();
    Integer tot_teachers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers_attendance);
        Intent intent = getIntent();


        try {
            AnalyticsEvent initiateAttendanceEvent = SessionManager.
                analytics.getEventClient().createEvent("Initiated Teacher Attendance");
            initiateAttendanceEvent.addAttribute("user",
                SessionManager.getInstance().getLogged_in_user());
            SessionManager.analytics.getEventClient().
                recordEvent(initiateAttendanceEvent);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Initiated Teacher Attendance");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Initiated Teacher Attendance");
        }

        String title = "Teacher Att " + intent.getStringExtra("date") + "/" +
            intent.getStringExtra("month") + "/" + intent.getStringExtra("year");
        this.setTitle(title);

        final ArrayList<TeacherListSource> teacher_list = new ArrayList<>();

        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        final String url =  server_ip + "/teachers/teacher_list/" + school_id + "/";
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        adapter  = new TeacherAttAdapter(activity,
            android.R.layout.simple_list_item_checked, intent);
        final ListView listView = findViewById(R.id.teacher_att_list);
        listView.setAdapter(adapter);

        String tag = "TeacherAttendance";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);
                            // get the id of the teacher
                            String id = jo.getString("id");
                            String f_name = jo.getString("first_name");
                            String l_name = jo.getString("last_name");
                            String email = jo.getString("email");
                            String mobile = jo.getString("mobile");

                            // put all the above details into the adapter
                            teacher_list.add(new TeacherListSource(id, f_name, l_name, mobile,
                                email));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch the list of students");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the list of students");
                            e.printStackTrace();
                        }
                    }
                    adapter.teacher_list = teacher_list;
                    tot_teachers = teacher_list.size();
                    progressDialog.hide();
                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("inside volley error handler");
                    progressDialog.hide();
                    progressDialog.dismiss();
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
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

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView checkBox = view.findViewById(R.id.txt_teacher_name);

                if (checkBox.isChecked()) {
                    // teacher has been marked as absent

                    adapter.absent_teachers.add(teacher_list.get(i).getId());
                    System.out.println ("unchecked adapter absent teachers list = ");
                    System.out.println (adapter.absent_teachers);

                    if (correction_list.contains(teacher_list.get(i).getId())) {
                        correction_list.remove(teacher_list.get(i).getId());
                        System.out.println ("unchecked correction list = ");
                        System.out.println (correction_list);
                    }
                    // teacher has been marked present.

                    adapter.notifyDataSetChanged();
                } else {
                    adapter.absent_teachers.remove(teacher_list.get(i).getId());
                    System.out.println ("checked adapter absent teachers list = ");
                    System.out.println (adapter.absent_teachers);

                    correction_list.add(teacher_list.get(i).getId());
                    System.out.println ("checked correction list = ");
                    System.out.println (correction_list);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0,
            "Submit").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                processAttendance(activity, adapter, correction_list, tot_teachers);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void processAttendance(final Activity activity,  final TeacherAttAdapter adapter,
                                   final ArrayList<String> correction_list, Integer tot_Teachers)
    {
        // Use the Builder class for convenient dialog construction
        Integer abs_count = adapter.absent_teachers.size();
        Integer pres = tot_Teachers - abs_count;
        String message = "Total: " + tot_Teachers.toString();
        message += "\nPresent: " + pres.toString() + ", Absent: " + abs_count.toString();
        message += "\nDo you want to Submit Teacher Attendance?";
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    JSONObject corrections = new JSONObject();
                    for (String c: correction_list)
                        try {
                            corrections.put(c, c);
                        } catch (JSONException je)  {
                            System.out.println
                                ("unable to create json for teacher's attendance correction");
                        }

                    JSONObject absentees = new JSONObject();
                    for (String a: adapter.absent_teachers) {
                        try {
                            absentees.put (a, a);
                        }   catch (JSONException je)  {
                            System.out.println
                                ("unable to create json for teacher's absentee list");
                        }
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put ("corrections", corrections);
                        jsonObject.put ("absentees", absentees);
                        System.out.println(jsonObject);
                    }   catch (JSONException je)  {
                        System.out.println
                            ("unable to create json for teacher's absence processing");
                    }

                    String tag = "Teacher Attendance processing";
                    String server_ip = MiscFunctions.getInstance().getServerIP(activity);
                    String school_id = SessionManager.getInstance().getSchool_id();
                    String url =  server_ip + "/teachers/process_attendance/" + school_id + "/";
                    url +=    getIntent().getStringExtra("date") + "/" +
                        getIntent().getStringExtra("month") + "/" +
                        getIntent().getStringExtra("year") + "/";
                    url = url.replace(" ", "%20");
                    JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.POST,
                        url, jsonObject, new Response.Listener<JSONObject>() {
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
                    com.classup.AppController.getInstance().addToRequestQueue(request2, tag);

                    adapter.absent_teachers.clear();
                    correction_list.clear();

                    Toast toast = Toast.makeText(getApplicationContext(),
                        "Teacher Attendance Submitted to server", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    try {
                        AnalyticsEvent conductedAttendanceEvent = SessionManager.
                            analytics.getEventClient().
                            createEvent("Conducted Teacher Attendance");
                        conductedAttendanceEvent.addAttribute("user",
                            SessionManager.getInstance().getLogged_in_user());
                        SessionManager.analytics.getEventClient().
                            recordEvent(conductedAttendanceEvent);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating analytics Conducted Attendance");
                    } catch (Exception exception)   {
                        System.out.println("flopped in creating analytics Conducted Attendance");
                    }

                    startActivity(new Intent("com.classup.SchoolAdmin").
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();

                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().pauseSession();
            SessionManager.analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().resumeSession();
        }
    }
}
