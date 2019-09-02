package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SelectStudent extends AppCompatActivity {
    final ArrayList<String> selected_students = new ArrayList<>();
    final Activity activity = this;
    String tag = "SelectStudents";
    String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);
        this.setTitle("Select Student(s)");

        sender = getIntent().getStringExtra("sender");

        final ArrayList<AttendanceListSource> student_list = new ArrayList<>();

        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        Intent intent = getIntent();
        final String student_list_url = server_ip + "/student/list/" + school_id + "/" +
            intent.getStringExtra("class") + "/" +
            intent.getStringExtra("section") + "/?format=json";
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final SelectStudentAdapter adapter = new SelectStudentAdapter(this, student_list,
            selected_students, sender);
        final ListView listView = findViewById(R.id.student_list);
        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(4);
        listView.setAdapter(adapter);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, student_list_url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);
                            // get the name of the student. We need to join first and last names
                            String f_name = jo.getString("fist_name");
                            String l_name = jo.getString("last_name");
                            String full_name = f_name + " " + l_name;
                            // get the erp_id of the student
                            String parent_name = jo.getString("parent");

                            // get the id of the student
                            String id = jo.getString("id");

                            // get the roll number of the student
                            String roll_no = jo.getString("roll_number");
                            // put all the above details into the adapter
                            student_list.add(new AttendanceListSource(roll_no,
                                full_name, id, parent_name));
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
                    progressDialog.hide();
                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("inside volley error handler");
                    progressDialog.hide();
                    progressDialog.dismiss();
                    if (error instanceof TimeoutError ||
                        error instanceof NoConnectionError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
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
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        // long tapping on student name will initiate call to parent
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int ii = i;
                final String student_name = student_list.get(i).getFull_name();
                android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(activity);
                final String message = "Do you want to call the parent of " + student_name +
                    "? Your number will be displayed on Parent's phone.";
                builder.setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // 12/09/17 - Now we are building the custom Analysis via AWS
                            try {
                                AnalyticsEvent callParentEvent =
                                    SessionManager.
                                        analytics.getEventClient().
                                        createEvent("Call Parent");
                                callParentEvent.addAttribute("user",
                                    SessionManager.getInstance().getLogged_in_user());
                                SessionManager.analytics.getEventClient().
                                    recordEvent(callParentEvent);
                            } catch (NullPointerException exception) {
                                System.out.println("flopped in creating " +
                                    "analytics Call Parent");
                            } catch (Exception exception) {
                                System.out.println("flopped in creating " +
                                    "analytics Call Parent");
                            }

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
                                                intent.setData(Uri.parse("tel:" + p_m1));
                                                System.out.println
                                                    ("going to make call");
                                                // check to see if dialler permssion exist
                                                int permissionCheck =
                                                    ContextCompat.checkSelfPermission
                                                        (activity, android.Manifest
                                                                .permission.CALL_PHONE);
                                                if (permissionCheck ==
                                                    PackageManager.PERMISSION_GRANTED)
                                                    startActivity(intent);
                                                else
                                                    Toast.makeText(
                                                        getApplicationContext(),
                                                        "Dialling permission not granted",
                                                        Toast.LENGTH_LONG).show();
                                            } catch (JSONException je) {
                                                System.out.println("Ran into " +
                                                    "JSON exception while trying to make call");
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
                                                    "Slow network connection or " +
                                                        "No internet connectivity",
                                                    Toast.LENGTH_LONG).show();
                                            }
                                        } else if (error instanceof ServerError) {
                                            Toast.makeText(getApplicationContext(),
                                                "Slow network connection or " +
                                                    "No internet connectivity",
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().pauseSession();
            SessionManager.analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().resumeSession();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        switch (sender) {
            case "share_image":
                // Inflate the menu; this adds items to the action bar if it is present.
                menu.add(0, 0, 0,
                    "Upload Image").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            default:
                menu.add(0, 0, 0,
                    "Compose Message").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                if (selected_students.size() == 0) {
                    String message = "Please select at least one student!";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    switch (sender) {
                        case "TeacherMenu":
                        case "send_message":
                            Intent intent1 = new Intent(activity, ComposeMessage.class);
                            intent1.putExtra("coming_from", "TeacherCommunication");
                            intent1.putExtra("student_list", selected_students);
                            intent1.putExtra("whole_class", "false");
                            startActivity(intent1);
                            break;
                        case "share_image":
                            String prompt = "Are you sure to upload this image?";

                            final android.app.AlertDialog.Builder builder =
                                new android.app.AlertDialog.Builder(this);
                            builder.setMessage(prompt).setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        final ProgressDialog progressDialog =
                                            new ProgressDialog(activity);
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        String timeStamp =
                                            new SimpleDateFormat("yyyyMMdd_HHmmss").
                                                format(new Date());
                                        String teacher = SessionManager.
                                            getInstance().getLogged_in_user();
                                        final String imageFileName = teacher + "-"
                                            + "_" + timeStamp + ".jpg";
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("image",
                                                SessionManager.getInstance().getImage());
                                            jsonObject.put("image_name", imageFileName);
                                            jsonObject.put("description",
                                                getIntent().
                                                    getStringExtra("brief_description"));
                                            jsonObject.put("school_id", SessionManager.
                                                getInstance().getSchool_id());
                                            jsonObject.put("teacher", teacher);
                                            jsonObject.put("class", getIntent().
                                                getStringExtra("class"));
                                            jsonObject.put("section",
                                                getIntent().getStringExtra("section"));
                                            jsonObject.put("whole_class", "false");
                                            jsonObject.put("student_list", selected_students);

                                        } catch (JSONException je) {
                                            System.out.println("unable to create json for Image upload");
                                            je.printStackTrace();
                                        } catch (ArrayIndexOutOfBoundsException ae) {
                                            ae.printStackTrace();
                                        }
                                        String server_ip = MiscFunctions.
                                            getInstance().getServerIP(activity);
                                        String url = server_ip + "/pic_share/upload_pic/";
                                        final String tag = "Upload Pic";
                                        JsonObjectRequest jsonObjReq = new JsonObjectRequest
                                            (Request.Method.POST, url, jsonObject,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        progressDialog.dismiss();
                                                        progressDialog.hide();
                                                        Log.d(tag, response.toString());
                                                        try {
                                                            final String status =
                                                                response.getString("status");
                                                            final String message =
                                                                response.getString("message");
                                                            if (!status.equals("success")) {
                                                                Toast toast =
                                                                    Toast.makeText(getApplicationContext(), message,
                                                                        Toast.LENGTH_LONG);
                                                                toast.setGravity(Gravity.CENTER,
                                                                    0,
                                                                    0);
                                                                toast.show();
                                                            } else {
                                                                Toast toast = Toast.makeText(getApplicationContext(),
                                                                    message, Toast.LENGTH_LONG);
                                                                toast.setGravity(Gravity.CENTER,
                                                                    0,
                                                                    0);
                                                                toast.show();
                                                                startActivity(new Intent
                                                                    ("com.classup.TeacherMenu").
                                                                    setFlags(Intent.
                                                                        FLAG_ACTIVITY_NEW_TASK |
                                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                                finish();
                                                            }
                                                        } catch (org.json.JSONException je) {
                                                            progressDialog.dismiss();
                                                            progressDialog.hide();
                                                            je.printStackTrace();
                                                        }
                                                    }
                                                }, new Response.ErrorListener() {

                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    progressDialog.dismiss();
                                                    progressDialog.hide();
                                                    VolleyLog.d(tag, "Error: " + error.getMessage());
                                                }
                                            });
                                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0,
                                            -1,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                        com.classup.AppController.getInstance().
                                            addToRequestQueue(jsonObjReq, tag);

                                        Toast toast = Toast.makeText(getApplicationContext(),
                                            "Image Upload in Progress. " +
                                                "It will appear in Image/Video list after a few minutes",
                                            Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();

                                        Intent intent1 = new Intent(getApplicationContext(),
                                            TeacherMenu.class);
                                        intent1.putExtra("sender", "teacher_menu");
                                        //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        //Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent1);
                                        //finish();
                                    }
                                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            // Create the AlertDialog object and return it
                            builder.show();
                            return super.onOptionsItemSelected(item);
                    }
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}