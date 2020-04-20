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
import android.widget.ListView;
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

public class OnlineQuestions extends AppCompatActivity {
    final Activity activity = this;
    String tag = "Online Classes";
    String server_ip;
    String school_id;
    String sender;
    String student_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_questions2);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        this.setTitle("Online Test List");


        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        final Intent intent = getIntent();
        student_id = intent.getStringExtra("student_id");
        String test_id = intent.getStringExtra("test_id");

        final ArrayList<OnlineQuestionSource> question_list = new ArrayList<>();
        final OnlineQuestionAdapter adapter = new OnlineQuestionAdapter(activity, question_list);
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

    }
}
