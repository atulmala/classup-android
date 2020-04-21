package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OnlineQuestions extends AppCompatActivity {
    final Activity activity = this;
    final Context context = this;
    String tag = "Online Classes";
    String server_ip;
    String school_id;
    String sender;
    String student_id;
    final ArrayList<StudentAnswers> student_answers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_questions2);
        final Intent intent = getIntent();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        String subject = intent.getStringExtra("subject");
        this.setTitle(subject);
        final TextView time_remaining = findViewById(R.id.time_remaining);


        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();

        student_id = intent.getStringExtra("student_id");
        String test_id = intent.getStringExtra("test_id");

        final ArrayList<OnlineQuestionSource> question_list = new ArrayList<>();

        final OnlineQuestionAdapter adapter = new OnlineQuestionAdapter(activity,
            question_list, student_answers);
        ListView listView = findViewById(R.id.online_questions);
        listView.setAdapter(adapter);


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Online Test Pleas wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        String url = server_ip + "/online_test/get_online_questions/" + test_id + "/?format=json";
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
                            student_answers.add(new StudentAnswers(student_id,
                                id, "X"));

                            String question = jo.getString("question");
                            String option_a = jo.getString("option_a");
                            String option_b = jo.getString("option_b");
                            String option_c = jo.getString("option_c");
                            String option_d = jo.getString("option_d");

                            // put all the above details into the adapter
                            question_list.add(new OnlineQuestionSource(id, i + 1, question,
                                option_a, option_b, option_c, option_d));
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

        new CountDownTimer(1 * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                String ms = String.format("%02d:%02d",
                    (millisUntilFinished / (60 * 1000)), ((millisUntilFinished / 1000) % 60));
                time_remaining.setText(ms);
            }

            public void onFinish() {
                time_remaining.setText(R.string.time_over);
                Toast toast = Toast.makeText(context, "Time over. now test will be Submittd",
                    Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait while Your Answers are submitted");
                progressDialog.setCancelable(false);
                progressDialog.show();
                JSONObject params = new JSONObject();
                for (int i = 0; i < student_answers.size(); i++) {
                    JSONObject params1 = new JSONObject();
                    try {
                        params1.put("student_id", student_answers.get(i).getStudent_id());
                        params1.put("question_id", student_answers.get(i).getQuestion_id());
                        params1.put("option_marked",
                            student_answers.get(i).getOption_marked());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        params.put(student_answers.get(i).getQuestion_id(), params1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println("params = " + params);
                }

                String URL = server_ip + "/online_test/submit_answers/";
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    URL, params,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(tag, response.toString());
                            try {
                                if (response.get("status").toString().equals("success")) {
                                    progressDialog.hide();
                                    progressDialog.cancel();
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                        "Answers Submitted. Result will be communicated later  ",
                                        Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER |
                                        Gravity.CENTER_HORIZONTAL
                                        | Gravity.CENTER_VERTICAL, 0, 0);
                                    toast.show();
                                    Intent intent = new Intent(context, ParentsMenu.class);
                                    intent.putExtra("sender", "ParentApp");
                                    intent.putExtra("student_id", student_id);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(tag, "Error: " + error.getMessage());
                    }
                });
                com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(context, "Please Complete the Test before going back",
            Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Submit")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.
            Builder(this);
        String prompt = "Are you sure you want to Submit Your Answers?";
        builder.setMessage(prompt).setPositiveButton("Yes", new DialogInterface.
            OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait while Your Answers are submitted");
                progressDialog.setCancelable(false);
                progressDialog.show();
                JSONObject params = new JSONObject();
                for (int i = 0; i < student_answers.size(); i++) {
                    JSONObject params1 = new JSONObject();
                    try {
                        params1.put("student_id", student_answers.get(i).getStudent_id());
                        params1.put("question_id", student_answers.get(i).getQuestion_id());
                        params1.put("option_marked",
                            student_answers.get(i).getOption_marked());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        params.put(student_answers.get(i).getQuestion_id(), params1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println("params = " + params);
                }

                String URL = server_ip + "/online_test/submit_answers/";
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    URL, params,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(tag, response.toString());
                            try {
                                if (response.get("status").toString().equals("success")) {
                                    progressDialog.hide();
                                    progressDialog.cancel();
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                        "Answers Submitted. Result will be communicated later  ",
                                        Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER |
                                        Gravity.CENTER_HORIZONTAL
                                        | Gravity.CENTER_VERTICAL, 0, 0);
                                    toast.show();
                                    Intent intent = new Intent(context, ParentsMenu.class);
                                    intent.putExtra("sender", "ParentApp");
                                    intent.putExtra("student_id", student_id);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(tag, "Error: " + error.getMessage());
                    }
                });
                com.classup.AppController.getInstance().addToRequestQueue(jsonObjReq, tag);
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
