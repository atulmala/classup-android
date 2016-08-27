package com.classup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ComposeMessage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String tag = "ComposeMessage";
        final String teacher = SessionManager.getInstance().getLogged_in_user();
        final String server_ip = MiscFunctions.getInstance().getServerIP(this);
        setContentView(R.layout.activity_compose_message);

        Button button = (Button) findViewById(R.id.btn_send_message_to_parents);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText);
                String message = editText.getText().toString();
                if (message.equals("")) {
                    Toast.makeText(getApplicationContext(), "Message is empty!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject jsonObject = new JSONObject();
                    Intent intent = getIntent();
                    ArrayList<String> selected_students = new ArrayList<>();
                    selected_students = intent.getStringArrayListExtra("student_list");

                    try {
                        jsonObject.put("message", message);
                        jsonObject.put("teacher", teacher);
                        jsonObject.put("class", intent.getStringExtra("class"));
                        jsonObject.put("section", intent.getStringExtra("section"));
                        if(intent.getStringExtra("whole_class").equals("true"))
                            jsonObject.put("whole_class", "true");
                        else
                            jsonObject.put("whole_class", "false");

                        if(intent.getStringExtra("whole_class").equals("false"))
                            for (int i=0; i<selected_students.size(); i++) {
                                jsonObject.put(MiscFunctions.getInstance().generateRandomString(),
                                        selected_students.get(i));
                            }
                    }
                    catch (JSONException je)  {
                        System.out.println("unable to create json for subjects to be deleted");
                        je.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        System.out.println("array out of bounds exception");
                        ae.printStackTrace();
                    }
                    String school_id = SessionManager.getInstance().getSchool_id();
                    String url =  server_ip + "/operations/send_message/" + school_id + "/";

                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                            url, jsonObject,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(tag, response.toString());
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(tag, "Error: " + error.getMessage());
                        }
                    });
                    com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
                    Toast.makeText(getApplicationContext(),
                            "Message(s) sent!",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent("com.classup.TeacherMenu").
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        });
    }
}
