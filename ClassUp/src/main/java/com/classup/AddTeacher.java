package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AddTeacher extends AppCompatActivity {
    final Context context = this;
    String server_ip;
    String school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);
        school_id = SessionManager.getInstance().getSchool_id();
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Add").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String user = SessionManager.getInstance().getLogged_in_user();
        final String employee_id = ((EditText)findViewById(R.id.employee_id)).getText().toString();
        final String full_name = ((EditText)findViewById(R.id.teacher_name)).getText().toString();
        final String mobile = ((EditText)findViewById(R.id.teacher_mobile)).getText().toString();
        final String teacher_login =
                ((EditText)findViewById(R.id.teacher_login)).getText().toString();

        int id = item.getItemId();

        switch (id) {
            case 0:
                // check for blanks
                if(employee_id.equals("")) {
                    Toast toast = Toast.makeText(this, "Employee ID is blank", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if(full_name.equals(""))    {
                    Toast toast = Toast.makeText(this, "Teacher Name is Blank", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if(mobile.equals(""))   {
                    Toast toast = Toast.makeText(this, "Mobile  is blank", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }
                if(teacher_login.equals(""))   {
                    Toast toast = Toast.makeText(this, "Teacher Login is blank", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                if(!MiscFunctions.getInstance().isValidEmailAddress(teacher_login)) {
                    Toast toast = Toast.makeText(this, "Login id is invalid.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                // mobile numbers should be of exactly 10 digits
                if(mobile.length() != 10) {
                    Toast toast = Toast.makeText(this,
                            "Mobile  is invalid. Should be of 10 digits", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return super.onOptionsItemSelected(item);
                }

                // send request to server to add this teacher
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
                        Builder(context);
                String prompt = "Are you sure you want to Add this teacher?";
                builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
                        OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("user", user);
                            jsonObject.put("school_id", school_id);
                            jsonObject.put("employee_id", employee_id);
                            jsonObject.put("full_name", full_name);
                            jsonObject.put("email", teacher_login);
                            jsonObject.put("mobile", mobile);
                        } catch (JSONException je) {
                            System.out.println("unable to create json for update student");
                            je.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException ae) {
                            ae.printStackTrace();
                        }

                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        final String tag = "AddTeacher";
                        server_ip = MiscFunctions.getInstance().getServerIP(context);
                        String url = server_ip + "/teachers/add_teacher/";

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
                                                                Toast.makeText(context, message,
                                                                        Toast.LENGTH_LONG);
                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                        toast.show();
                                                    } else {
                                                        Toast toast = Toast.makeText(context,
                                                                message, Toast.LENGTH_LONG);
                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                        toast.show();
                                                        startActivity(new Intent
                                                                ("com.classup.SchoolAdmin").
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
                        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0, -1,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        com.classup.AppController.getInstance().
                                addToRequestQueue(jsonObjReq, tag);

                    }
                }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
