package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectStudent extends AppCompatActivity {
    String tag = "SelectStudents";
    final ArrayList<String> selected_students = new ArrayList<>();
    final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);
        //final Activity activity = this;

        final ArrayList<AttendanceListSource> student_list = new ArrayList<AttendanceListSource>();
        //final ArrayList<String> selected_students = new ArrayList<>();

        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        final String school_id = SessionManager.getInstance().getSchool_id();
        String logged_in_user = SessionManager.getInstance().getLogged_in_user();
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
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("inside volley error handler");
                        progressDialog.hide();
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

        /*Button button = (Button) findViewById(R.id.btn_compose_message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });*/
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