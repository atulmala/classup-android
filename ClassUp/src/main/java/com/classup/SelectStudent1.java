package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectStudent1 extends AppCompatActivity {
    final Activity activity = this;
    String tag = "SelectStudents1";;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student1);

        final ArrayList<AttendanceListSource> student_list = new ArrayList<AttendanceListSource>();

        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        final Intent intent = getIntent();
        final String student_list_url =  server_ip + "/student/list/" + school_id + "/" +
                intent.getStringExtra("class") + "/" +
                intent.getStringExtra("section") + "/?format=json";
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final SelectStudent1Adapter adapter = new SelectStudent1Adapter(this,
                android.R.layout.simple_list_item_checked, student_list);
        final ListView listView = (ListView) findViewById(R.id.student_list1);
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
                                String r_no = jo.getString("roll_number");
                                String f_name = jo.getString("fist_name");
                                String l_name = jo.getString("last_name");
                                String full_name = r_no + "    " + f_name + " " + l_name;
                                // get the erp id of the student
                                String erp_id = jo.getString("student_erp_id");

                                // get the id of the student
                                String id = jo.getString("id");

                                // get the roll number of the student
                                String roll_no = jo.getString("roll_number");
                                // put all the above details into the adapter
                                student_list.add(new AttendanceListSource(roll_no,
                                        full_name, id, erp_id));
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
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
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

        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String student_id = student_list.get(i).getId();
                Intent intent1 = new Intent(activity, EditStudent.class);
                intent1.putExtra("student_id", student_id);
                startActivity(intent1);
            }
        });
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
}