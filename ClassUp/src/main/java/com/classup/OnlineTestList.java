package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OnlineTestList extends AppCompatActivity {
    final Activity activity = this;
    String tag = "Online Classes";
    String server_ip;
    String school_id;
    String sender;
    String student_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_test_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        this.setTitle("Online Test List");


        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();

        final ArrayList<OnlineTestSource> test_list = new ArrayList<>();

        ListView listView = findViewById(R.id.online_test);
        final OnlineTestAdapter adapter = new OnlineTestAdapter(activity, test_list);
        listView.setAdapter(adapter);

        student_id = getIntent().getStringExtra("student_id");
        String url = server_ip + "/online_test/get_online_test/" + student_id + "/?format=json";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Online Test Pleas wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() < 1) {
                        Toast toast = Toast.makeText(c, "No Online Tests Scheduled",
                            Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);
                            String id = jo.getString("id");

                            String date = jo.getString("date");
                            String yy = date.substring(0, 4);
                            String month = date.substring(5, 7);
                            String dd = date.substring(8, 10);
                            String ddmmyyyy = dd + "/" + month + "/" + yy;

                            String the_class = jo.getString("the_class");

                            String subject = jo.getString("subject");

                            int duration = jo.getInt("duration");

                            // put all the above details into the adapter
                            test_list.add(new OnlineTestSource(id, ddmmyyyy, subject,
                                the_class, duration));
                            adapter.notifyDataSetChanged();

                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch the Online Classes list");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the HW/Image list");
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
                        Toast.makeText(c, "Slow network connection",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(c, "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(c, "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }
                    // TODO Auto-generated method stub
                }
            });
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        final Intent intent = new Intent(this, OnlineQuestions.class);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                String test_id = test_list.get(i).getId();
                String url = server_ip + "/online_test/whether_attempted/" +
                    student_id + "/" + test_id + "/?format=json";

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
                                    } else {
                                        intent.putExtra("test_id", test_list.get(i).getId());
                                        intent.putExtra("student_id", student_id);
                                        startActivity(intent);
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
                int socketTimeout = 300000;//5 minutes
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                    -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jsObjRequest1.setRetryPolicy(policy);
                com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);


            }
        });
    }
}
