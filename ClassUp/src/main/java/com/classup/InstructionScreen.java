package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InstructionScreen extends AppCompatActivity {
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_screen);
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 0, 0, "Start Test").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
            Builder(this);
        String prompt = "Have you carefully read all Instructions? " +
            "Are you sure you want to Start this Online Test?";
        builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
            OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait test loads");
                Intent intent = getIntent();
                final String test_id = intent.getStringExtra("test_id");
                final String student_id = intent.getStringExtra("student_id");
                String server_ip = MiscFunctions.getInstance().getServerIP(context);

                String url = server_ip + "/online_test/whether_attempted/" +
                    student_id + "/" + test_id + "/?format=json";
                final Intent intent1 = new Intent(context, OnlineQuestions.class);
                JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
                    (Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String result = (response.get("attempted")).
                                        toString();
                                    if (result.equals("true")) {
                                        String message = "You have already attempted this test";
                                        Toast toast = Toast.makeText(getApplicationContext(), message,
                                            Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    } else {
                                        intent1.putExtra("test_id", test_id);
                                        intent1.putExtra("student_id", student_id);
                                        startActivity(intent1);
                                    }
                                } catch (org.json.JSONException je) {
                                    je.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError ||
                                error instanceof NoConnectionError) {
                                if (!MiscFunctions.getInstance().checkConnection
                                    (getApplicationContext())) {
                                    Toast.makeText(getApplicationContext(),
                                        "Slow network connection " +
                                            "or No internet connectivity",
                                        Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                        "Some problem at server end, " +
                                            "please try after some time",
                                        Toast.LENGTH_LONG).show();
                                }
                            } else if (error instanceof ServerError) {
                                Toast.makeText(getApplicationContext(),
                                    "User does not exist. Please contact " +
                                        "ClassUp Support at support@classup.in",
                                    Toast.LENGTH_LONG).show();
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(getApplicationContext(),
                                    "Network error, please try later",
                                    Toast.LENGTH_LONG).show();
                            } else if (error instanceof ParseError) {
                                //TODO
                            }
                            System.out.println("inside volley error handler(LoginActivity)");
                            // TODO Auto-generated method stub
                        }
                    });
                com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);
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
