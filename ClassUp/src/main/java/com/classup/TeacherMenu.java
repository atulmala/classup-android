package com.classup;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class  TeacherMenu extends AppCompatActivity {
    final ArrayList<String> bus_attendance = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    private void setUpVariables()   {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpVariables();
        setContentView(R.layout.activity_teacher_menu1);

        String server_ip = MiscFunctions.getInstance().getServerIP(getApplicationContext());
        String school_id = SessionManager.getInstance().getSchool_id();
        if (!SessionManager.getInstance().getBus_attendance_known()) {
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
                                    bus_attendance.add(jo.getString("enable_bus_attendance"));
                                    SessionManager.getInstance().setBus_attendance_known(true);
                                    SessionManager.getInstance().
                                            setBus_attendance(jo.getString("enable_bus_attendance"));
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
            com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);
        }
        else    {
            bus_attendance.add(SessionManager.getInstance().getBus_attendance());
        }

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

    public void ExamManagement(View view)   {
        Intent intent = new Intent(this, ExamTestManagement.class);
        startActivity(intent);
    }

    public void online_classes(View view)    {
        Intent intent = new Intent(this, OnlineClasses.class);
        intent.putExtra("sender", "teacher");
        startActivity(intent);
    }

    public void setSubjects(View view)  {
        Intent intent = new Intent(this, SetSubjects.class);
        startActivity(intent);
    }

    public void summaryAttendance(View view)    {
        Intent intent = new Intent(this, SelectCriteriaAttendanceSummary.class);
        startActivity(intent);
    }

    public void changePassword()   {
        Intent intent = new Intent(this, PasswordChange.class);
        startActivity(intent);
    }


    public void commCenter (View view) {
        Intent intent = new Intent(this, CommunicationCenter.class);
        startActivity (intent);
    }

    public void manageHW(View view) {
        // check for storage access permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            Intent intent = new Intent(this, HWList.class);
            intent.putExtra("sender", "teacher_menu");
            startActivity(intent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String message = "Thanks for granting the permission, " +
                        "this functionality will now be available";
                    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    String message = "As you have not granted the required permission, " +
                        "this functionality will not be available";
                    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
                return;
            }

        }
    }
    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Logout").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        m.add(0, 1, 0, "Change Password").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        switch (id) {
            case 0:
                SessionManager.getInstance().logout();
                finishAndRemoveTask ();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case 1:
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
                        Builder(this);
                String prompt = "Are you sure you want to change password?";
                builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                        OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        changePassword();
                    }
                }).setNegativeButton(R.string.cancel,  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                // Create the AlertDialog object and return it
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
