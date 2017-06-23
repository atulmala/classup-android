package com.classup;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class AttendanceList extends AppCompatActivity {

    AttendanceListAdapter ptr_adapter;
    String tag = "AttendanceList";
    String server_ip;
    String school_id;
    final Activity activity = this;

    int tot_students = 0;
    private void clearAbsentee_list(AttendanceListAdapter adapter) {
        adapter.clearAbsentee_list();
    }
    private void clearCorrection_List(AttendanceListAdapter adapter)    {
        adapter.clearCorrectionList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isConnected = MiscFunctions.getInstance().checkConnection(getApplicationContext());
        if (!isConnected)   {
            final Context context = getApplicationContext();
            String text = "Looks you are not connected to internet. Please connect and try again";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            return;
        }
        final Intent intent = getIntent();
        Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();

        final String student_list_url =  server_ip + "/student/list/" +
                school_id + "/" + intent.getStringExtra("class") + "/" +
                intent.getStringExtra("section") + "/?format=json";

        // 23/06/2017 - show class subject and date on the Action bar like we do on iOS
        String title = intent.getStringExtra("class") + "-" + intent.getStringExtra("section");
        title +=  " " + intent.getStringExtra("date") + "/" +
                intent.getStringExtra("month") + "/" + intent.getStringExtra("year");
        title += " " + intent.getStringExtra("subject");
        this.setTitle(title);


        setContentView(R.layout.activity_attendance_list);

        final ArrayList<AttendanceListSource> attendanceList = new ArrayList<AttendanceListSource>();
        final ListView view = (ListView) findViewById(R.id.attendance_list);
        final AttendanceListAdapter adapter =
                new AttendanceListAdapter(this, attendanceList, intent);

        ptr_adapter = adapter;

        view.setAdapter(adapter);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
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
                                String erp_id = jo.getString("student_erp_id");

                                // get the id of the student
                                String id = jo.getString("id");

                                // get the roll number of the student
                                String roll_no = jo.getString("roll_number");

                                // 01/06/2017 get the parent name
                                String parent_name = jo.getString("parent");

                                // put all the above details into the adapter
                                attendanceList.add(new AttendanceListSource
                                        (roll_no, full_name, id, parent_name));
                                tot_students = attendanceList.size();
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
                                        "Some problem at server end, please try after some time",
                                        Toast.LENGTH_LONG).show();
                            }
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Server error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {

                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                    }
                });
        // here we can sort the attendance list as per roll number

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        // on long pressing a student name, pop up will appear showing parent's name and
        // mobile number
        view.setLongClickable(true);
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()   {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int ii = i;
                final String student_name = attendanceList.get(i).getFull_name();
                android.app.AlertDialog.Builder builder =
                        new android.app.AlertDialog.Builder(activity);
                builder.setMessage("Do you want to call the parent of  " + student_name + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String student_id = attendanceList.get(ii).getId();
                                String server_ip = MiscFunctions.getInstance().
                                        getServerIP(activity);
                                String url = server_ip + "/student/get_parent/" + student_id + "/";
                                url = url.replace(" ", "%20");
                                progressDialog.show();
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                        (Request.Method.GET, url, null,
                                                new Response.Listener<JSONObject>()  {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    //JSONObject jo = response.getJSONObject;

                                                    String p_m1 = response.get("parent_mobile1").
                                                            toString();
                                                    System.out.println("mobile=" + p_m1);
                                                    Intent intent = new Intent(Intent.ACTION_CALL);
                                                    intent.setData(Uri.parse("tel:" + p_m1));
                                                    System.out.println("going to make call");
                                                    // check to see if dialler permssion exist
                                                    int permissionCheck =
                                                            ContextCompat.checkSelfPermission
                                                            (activity,
                                                            Manifest.permission.CALL_PHONE);
                                                    if(permissionCheck==
                                                            PackageManager.PERMISSION_GRANTED)
                                                        startActivity(intent);
                                                    else
                                                        Toast.makeText(getApplicationContext(),
                                                                "Dialling permission not granted",
                                                                Toast.LENGTH_LONG).show();

                                                } catch (JSONException je) {
                                                    System.out.println("Ran into JSON exception " +
                                                            "while trying to make call");
                                                    je.printStackTrace();
                                                } catch (Exception e) {
                                                    System.out.println("Caught General exception " +
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
                                                    if(!MiscFunctions.getInstance().checkConnection
                                                            (getApplicationContext())) {
                                                        Toast.makeText(getApplicationContext(),
                                                                "Slow network connection or " +
                                                                        "No internet connectivity",
                                                                Toast.LENGTH_LONG).show();
                                                    } else  {
                                                        Toast.makeText(getApplicationContext(),
                                                                "Some problem at server end, " +
                                                                        "please " +
                                                                        "try after some time",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }  else if (error instanceof ServerError) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Server error, please try later",
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

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Submit").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //menu.add(0, 1, 0, "Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                processAttendance(getIntent(), ptr_adapter, tot_students);
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

    public void processAttendance(final Intent intent, final AttendanceListAdapter adapter,
                                  int tot_students)
    {
        // create the dialog box for confirmation
        final Dialog dialog = new Dialog(AttendanceList.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_attendance_submission1);

        // show the date of attendance
        TextView txt_date = (TextView) dialog.findViewById(R.id.txt_att_submission_date);
        final String d = intent.getStringExtra("date");
        final String m = intent.getStringExtra("month"); // show the dialog that attendance has successfully been submitted

        final String y = intent.getStringExtra("year");

        String formatted_date = d + "/" + m + "/" + y;
        txt_date.setText(formatted_date);
        txt_date.setTypeface(Typeface.DEFAULT_BOLD);

        // show class & section
        TextView txt_class_section =
                (TextView) dialog.findViewById((R.id.txt_att_submission_class));
        String class_plus_section =
                intent.getStringExtra("class") + " " + intent.getStringExtra("section");
        txt_class_section.setText(class_plus_section);
        txt_class_section.setTypeface(Typeface.DEFAULT_BOLD);

        // show the subject
        TextView txt_subject =
                (TextView) dialog.findViewById(R.id.txt_att_submission_subject);
        txt_subject.setText(intent.getStringExtra("subject"));
        txt_subject.setTypeface(Typeface.DEFAULT_BOLD);

        // get the list of absentee
        final List<String> absentee_list = adapter.getAbsentee_list();

        // 03/06/2017 - show the total count
        TextView txt_total = (TextView) dialog.findViewById(R.id.txt_att_submission_total);
        Integer tot_stu = tot_students;
        txt_total.setText(tot_stu.toString());

        // show absent count
        Integer absent_count = absentee_list.size();
        TextView txt_absent = (TextView) dialog.findViewById((R.id.txt_att_absent_count));
        txt_absent.setText(absent_count.toString());
        txt_absent.setTypeface(Typeface.DEFAULT_BOLD);

        // show present count
        Integer present_count = tot_students - absent_count;
        TextView txt_present_count =
                (TextView) dialog.findViewById(R.id.txt_att_present_count);
        txt_present_count.setText(present_count.toString());
        txt_present_count.setTypeface(Typeface.DEFAULT_BOLD);

        // now, show the dialog
        dialog.show();

        // what happens when OK button is clicked?
        Button btn_ok = (Button) dialog.findViewById(R.id.btn_att_confirm);

        final String teacher = SessionManager.getInstance().getLogged_in_user();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update the server tables to indicate that the attendance for this
                // class/section/subject/date was taken
                String url =  server_ip + "/attendance/attendance_taken/" + school_id +
                        "/" + intent.getStringExtra("class") + "/" +
                        intent.getStringExtra("section") + "/" +
                        intent.getStringExtra("subject") + "/" +
                        d + "/" + m + "/" + y + "/" + teacher + "/";
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
                                Context context = getApplicationContext();
                                String text = "Looks there is a problem with connection" +
                                        " or at the server end. Please check and try again";
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
                for (String id : absentee_list) {
                    try {
                        jsonObject.put(id, id);
                    } catch (JSONException je) {
                        System.out.println("unable to create json for " +
                                "absentees to be deleted");
                        je.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        System.out.println("array out of bounds exception");
                        ae.printStackTrace();
                    }
                }
                String url1 =  server_ip + "/attendance/update1/" + school_id + "/" +
                        intent.getStringExtra("class") + "/" +
                        intent.getStringExtra("section") + "/" +
                        intent.getStringExtra("subject") + "/" +
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
                com.classup.AppController.getInstance().addToRequestQueue(request1, tag);

                // now send the correction request. Means the student who are to be marked
                // present from absent
                final  List<String> correction_list = adapter.getCorrection_list();
                JSONObject jsonObject2 = new JSONObject();
                try {
                    for (String id : correction_list)
                        jsonObject2.put(id, id);
                    System.out.println("correction list to be sent to server=" + correction_list);
                } catch (JSONException je) {
                    System.out.println("unable to create json for correction");
                    je.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException ae) {
                    System.out.println("array out of bounds exception");
                    ae.printStackTrace();
                }
                String url2 =  server_ip + "/attendance/delete2/" + school_id + "/" +
                        intent.getStringExtra("class") + "/" +
                        intent.getStringExtra("section") + "/" +
                        intent.getStringExtra("subject") + "/" + d + "/" + m + "/" + y + "/";
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
                com.classup.AppController.getInstance().addToRequestQueue(request2, tag);

                dialog.dismiss();
                // clear the absentee list & correction list
                clearAbsentee_list(ptr_adapter);
                clearCorrection_List(ptr_adapter);
                // show the toast that attendance has successfully been submitted
                Toast.makeText(getApplicationContext(), "Attendance submitted to server",
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent("com.classup.TeacherMenu").
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        // processing for Cancel button
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_att_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //startActivity(new Intent("com.classup.TeacherMenu"));
            }
        });
    }
}
