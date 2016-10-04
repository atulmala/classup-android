package com.classup;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectStudent extends AppCompatActivity {
    final ArrayList<String> selected_students = new ArrayList<>();
    final Activity activity = this;
    String tag = "SelectStudents";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);
        //final Activity activity = this;

        final ArrayList<AttendanceListSource> student_list = new ArrayList<AttendanceListSource>();
        //final ArrayList<String> selected_students = new ArrayList<>();

        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        Intent intent = getIntent();
        final String student_list_url =  server_ip + "/student/list/" + school_id + "/" +
                intent.getStringExtra("class") + "/" +
                intent.getStringExtra("section") + "/?format=json";
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final SelectStudentAdapter adapter = new SelectStudentAdapter(this,
                android.R.layout.simple_list_item_checked, student_list);
        final ListView listView = (ListView) findViewById(R.id.student_list);
        listView.setDivider(new ColorDrawable(0x99F10529));
        listView.setDividerHeight(1);
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

                                // get the id of the student
                                String id = jo.getString("id");

                                // get the roll number of the student
                                String roll_no = jo.getString("roll_number");
                                // put all the above details into the adapter
                                student_list.add(new AttendanceListSource(roll_no, full_name, id));
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
                                    "Slow network connection, please try later",
                                    Toast.LENGTH_LONG).show();
                        }  else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),
                                    "Server error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),
                                    "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                        }
                        // TODO Auto-generated method stub
                    }
                });
        // here we can sort the attendance list as per roll number

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.student_name);

                if (!textView.isChecked()) {
                    textView.setChecked(true);

                    selected_students.add(student_list.get(i).getId());

                    // also add to the selected subjects list of the adapter
                    adapter.selected_students.add(student_list.get(i).getId());
                    adapter.notifyDataSetChanged();

                } else {
                    textView.setChecked(false);

                    // also remove from the selected subjects list of the adapter
                    adapter.selected_students.remove(student_list.get(i).getId());
                    adapter.notifyDataSetChanged();

                    if (selected_students.contains(student_list.get(i).getId())) {
                        selected_students.remove(student_list.get(i).getId());
                        adapter.selected_students.remove((student_list.get(i).getId()));
                    }
                }
            }
        });

        // long tapping on student name will initiate call to parent
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
                                                                "Some problem at server end, " +
                                                                        "please " +
                                                                        "try after some time",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                } else if (error instanceof ServerError) {
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

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Compose Message").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                if (selected_students.size() == 0)  {
                    String message = "Please select at least one student!";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent1 = new Intent(activity, ComposeMessage.class);
                    intent1.putExtra("student_list", selected_students);
                    intent1.putExtra("whole_class", "false");
                    startActivity(intent1);
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}