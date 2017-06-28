package com.classup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

import java.util.ArrayList;

public class ComposeMessage extends AppCompatActivity {
    final Activity a = this;
    final String tag = "ComposeMessage";
    final String teacher = SessionManager.getInstance().getLogged_in_user();
    final String server_ip = MiscFunctions.getInstance().getServerIP(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));


    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Send Message").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case 0:
                EditText editText = (EditText) findViewById(R.id.editText);
                final String message = editText.getText().toString();
                if (message.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Message is empty!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else if (message.length() > 200) {
                    String prompt = "Message is too long. Please limit it to 140 Characters";
                    Toast toast = Toast.makeText(getApplicationContext(),
                            prompt, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    // 10/01/17 - Upon request from GRADS International School teachers,
                    // we are adding confirmation dialog prior to sending the message to prevent
                    // accidental sending
                    final android.app.AlertDialog.Builder builder =
                            new android.app.AlertDialog.Builder(a);
                    builder.setMessage("Are you sure you want to send message(s)?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    JSONObject jsonObject = new JSONObject();
                                    Intent intent = getIntent();
                                    ArrayList<String> selected_students = new ArrayList<>();
                                    selected_students =
                                            intent.getStringArrayListExtra("student_list");

                                    try {
                                        jsonObject.put("message", message);
                                        jsonObject.put("teacher", teacher);
                                        jsonObject.put("class", intent.getStringExtra("class"));
                                        jsonObject.put("section", intent.getStringExtra("section"));
                                        if (intent.getStringExtra("whole_class").equals("true"))
                                            jsonObject.put("whole_class", "true");
                                        else
                                            jsonObject.put("whole_class", "false");

                                        if (intent.getStringExtra("whole_class").equals("false")) {
                                            for (int i = 0; i < selected_students.size(); i++) {
                                                jsonObject.put(MiscFunctions.getInstance().
                                                                generateRandomString(),
                                                        selected_students.get(i));
                                            }
                                            System.out.println(jsonObject);
                                        }
                                    } catch (JSONException je) {
                                        System.out.println
                                                ("unable to create json for " +
                                                        "compose message functionality");
                                        je.printStackTrace();
                                    } catch (ArrayIndexOutOfBoundsException ae) {
                                        System.out.println("array out of bounds exception");
                                        ae.printStackTrace();
                                    }
                                    String school_id = SessionManager.getInstance().getSchool_id();
                                    String url =
                                            server_ip + "/operations/send_message/" +
                                                    school_id + "/";

                                    JsonObjectRequest jsonObjReq =
                                            new JsonObjectRequest(Request.Method.POST,
                                                    url, jsonObject,
                                                    new Response.Listener<JSONObject>() {

                                                        @Override
                                                        public void onResponse(JSONObject response) {
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
                                    Toast.makeText(getApplicationContext(),
                                            "Message(s) sent!",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent("com.classup.TeacherMenu").
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
                    return super.onOptionsItemSelected(item);
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
