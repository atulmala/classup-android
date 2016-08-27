package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.classup.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class TeacherMenu extends AppCompatActivity {
    private Button btnTakeAttendance;
    private Button btnManageTest;

    final ArrayList<String> bus_attendance = new ArrayList<>();

    private void setUpVariables()   {
        btnTakeAttendance = (Button)findViewById(R.id.btn_Attendance);
        btnManageTest = (Button)findViewById(R.id.btn_schedule_Test);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpVariables();
        setContentView(R.layout.activity_teacher_menu);

        String server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
        String school_id = SessionManager.getInstance().getSchool_id();
        String url = server_ip + "/setup/bus_attendance_enabled/" + school_id + "/?format=json";
        final String tag = "bus_attendance_enabled";
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                bus_attendance.add(jo.getString("enable_bus_attendance")); ;
                            } catch (JSONException je) {
                                System.out.println("Ran into JSON exception while dealing with "
                                        + tag);
                                je.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Caught General exception " +
                                        "while dealing with" + tag);
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
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

    }

    // selecting date, class, section, and subject is a common task for taking attendance and
    // scheduling tests. Hence we are going to use the same activity. The differentiation will
    // be done by intent parameter sender which will be set to takeAttendance or scheduleTest
    public void takeAttendance(View view)    {
        Intent intent = new Intent(this, SelectClass.class);
        System.out.println("intent=" + intent);
        intent.putExtra("sender", "takeAttendance");
        System.out.println("intent set to takeAttendance");
        startActivity(intent);
        //startActivity(new Intent("com.classup.SelectClass"));
    }

    public void selectBusRout(View view)    {
        if(bus_attendance.get(0).equals("true")) {
            Intent intent = new Intent(this, SelectCriteriaBusAttendance.class);
            startActivity(intent);
        }
        else    {
            Toast.makeText(getApplicationContext(), "This functionality is not available for " +
                    "your account. Please contact ClassUp Support to get this functionality.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void scheduleTest(View view) {
        Intent intent = new Intent(this, SelectClass.class);
        intent.putExtra("sender", "scheduleTest");
        System.out.println("intent set to scheduleTest");
        startActivity(intent);
    }

    public void manageTest(View view)   {
        Intent intent = new Intent(this, TestManagerActivity.class);
        startActivity(intent);
    }

    public void setSubjects(View view)  {
        Intent intent = new Intent(this, SetSubjects.class);
        startActivity(intent);
    }

    public void summaryAttendance(View view)    {
        Intent intent = new Intent(this, SelectAttendanceSummaryCriteria.class);
        startActivity(intent);
    }

    public void changePassword(View view)   {
        Intent intent = new Intent(this, PasswordChange.class);
        startActivity(intent);
    }

    public void performLogout(View view)    {
        SessionManager.getInstance().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void sendMessage(View view)  {
        Intent intent = new Intent(this, SelectClassSection.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_teacher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
