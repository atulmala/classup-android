package com.classup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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

import java.util.ArrayList;

public class SendBulkSMS extends AppCompatActivity {
    final Activity a = this;
    final String tag = "BulkSMSFromDevice";
    final String server_ip = MiscFunctions.getInstance().getServerIP(this);
    final String user = SessionManager.getInstance().getLogged_in_user();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_bulk_sms);
    }

    public void selectClasses(View view)    {
        EditText editText = (EditText)findViewById(R.id.bulkSMS);
        final String message = editText.getText().toString();

        if (message.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(), "Message is empty!",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (message.length() > 200) {
            String prompt = "Message is too long. Please limit it to 200 Characters";
            Toast toast = Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Intent intent = new Intent(this, SelectionForBulkSMS.class);
            intent.putExtra("message", message);
            startActivity(intent);
        }
    }

    public void bulkSMSwholeSchool(View view)    {
        EditText editText = (EditText)findViewById(R.id.bulkSMS);
        final String message = editText.getText().toString();

        // check for empty message
        if (message.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(), "Message is empty!",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (message.length() > 140) {
            String prompt = "Message is too long. Please limit it to 140 Characters";
            Toast toast = Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
                final android.app.AlertDialog.Builder builder =
                        new android.app.AlertDialog.Builder(a);
                builder.setMessage("Are you sure you want to send message(s)?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("user", user);
                                    jsonObject.put("from_device", "true");
                                    jsonObject.put("whole_school", "true");
                                    jsonObject.put("message_text", message);
                                    String school_id = SessionManager.getInstance().getSchool_id();
                                    jsonObject.put("school_id", school_id);
                                } catch (JSONException je) {
                                    System.out.println
                                            ("unable to create json for " +
                                                    "subjects to be deleted");
                                    je.printStackTrace();
                                } catch (ArrayIndexOutOfBoundsException ae) {
                                    System.out.println("array out of bounds exception");
                                    ae.printStackTrace();
                                }
                                String url =
                                        server_ip + "/operations/send_bulk_sms/";

                                JsonObjectRequest jsonObjReq =
                                        new JsonObjectRequest(Request.Method.POST,
                                                url, jsonObject,
                                                new Response.Listener<JSONObject>() {

                                                    @Override
                                                    public void onResponse(JSONObject response)
                                                    {
                                                        Log.d(tag, response.toString());
                                                    }
                                                }, new Response.ErrorListener() {

                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                VolleyLog.d(tag, "Error: "
                                                        + error.getMessage());
                                            }
                                        });
                                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(0, -1,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                com.classup.AppController.getInstance().
                                        addToRequestQueue(jsonObjReq, tag);
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Message(s) sent!", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                startActivity(new Intent("com.classup.SchoolAdmin").
                                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();

            }
        }
    }

