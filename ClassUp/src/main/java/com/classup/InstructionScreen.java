package com.classup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class InstructionScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_screen);
    }

    public void start_test (final View view)  {
        Intent intent = getIntent();
        final String test_id = intent.getStringExtra("test_id");
        final String student_id = intent.getStringExtra("student_id");
        String server_ip = MiscFunctions.getInstance().getServerIP(this);

        String url = server_ip + "/online_test/whether_attempted/" +
            student_id + "/" + test_id + "/?format=json";
        final Intent intent1 = new Intent(this, OnlineQuestions.class);
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
                                Toast.makeText(getApplicationContext(), message,
                                    Toast.LENGTH_SHORT).show();
                                view.setEnabled(false);
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
}
