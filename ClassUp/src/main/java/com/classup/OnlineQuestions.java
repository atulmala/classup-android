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
import com.android.volley.RetryPolicy;
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
    String time_remaining = "Time Remaining";
    final ArrayList<StudentAnswers> student_answers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_questions2);
        final Intent intent = getIntent();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        this.setTitle(time_remaining);

        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();

        student_id = intent.getStringExtra("student_id");
        String test_id = intent.getStringExtra("test_id");
        int duration = SessionManager.getInstance().getTest_duration();
        final ArrayList<OnlineQuestionSource> question_list = new ArrayList<>();

        final OnlineQuestionAdapter adapter = new OnlineQuestionAdapter(activity,
            question_list, student_answers);
        ListView listView = findViewById(R.id.online_questions);
        listView.setAdapter(adapter);


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Online Test Pleas wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        String url1 = server_ip + "/online_test/mark_attempted/" + student_id + "/" + test_id + "/";
        JsonObjectRequest jsObjRequest1 = new JsonObjectRequest
            (Request.Method.POST, url1, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        com.classup.AppController.getInstance().addToRequestQueue(jsObjRequest1);

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
                            option_a = getRidofDecimal(option_a);

                            String option_b = jo.getString("option_b");
                            option_b = getRidofDecimal(option_b);

                            String option_c = jo.getString("option_c");
                            option_c = getRidofDecimal(option_c);

                            String option_d = jo.getString("option_d");
                            option_d = getRidofDecimal(option_d);

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

        new CountDownTimer(duration * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                String ms = String.format("Time Remaining: %02d:%02d",
                    (millisUntilFinished / (60 * 1000)), ((millisUntilFinished / 1000) % 60));
                activity.setTitle(ms);
            }

            public void onFinish() {
                activity.setTitle("Time Over!");
                Toast toast = Toast.makeText(context, "Time over. now test will be Submittd",
                    Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait while Your Answers are submitted");
                progressDialog.setCancelable(false);
                progressDialog.show();

                progressDialog.hide();
                progressDialog.cancel();
                Toast toast1 = Toast.makeText(getApplicationContext(),
                    "Answers Submitted. Result will be communicated later  ",
                    Toast.LENGTH_SHORT);
                toast1.setGravity(Gravity.CENTER |
                    Gravity.CENTER_HORIZONTAL
                    | Gravity.CENTER_VERTICAL, 0, 0);
                toast1.show();
                Intent intent = new Intent(context, ParentsMenu.class);
                intent.putExtra("sender", "ParentApp");
                intent.putExtra("student_id", student_id);
                startActivity(intent);
                finish();

            }
        }.start();
    }

    private String getRidofDecimal(String option)  {
        try {
            String last_2 = option.length() > 2 ?
                option.substring(option.length() - 2) : option;
            if (last_2.equals(".0"))  {
                return option.substring(0, option.length() - 2);
            }
            else    {
                return option;
            }
        }
        catch (Exception e) {
            return option;
        }
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
