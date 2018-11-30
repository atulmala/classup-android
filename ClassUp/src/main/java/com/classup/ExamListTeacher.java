package com.classup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
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

import java.util.ArrayList;

public class ExamListTeacher extends AppCompatActivity {
    private String selected_exam_title = "nil";
    private String selected_exam_id = "nil";
    private String select_exam_type = "nil";
    private  String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_list_teacher);
        this.setTitle("Select Exam");

        Intent intent = getIntent();
        sender = intent.getStringExtra("sender");

        try {
            AnalyticsEvent event =
                SessionManager.getInstance().analytics.getEventClient().
                    createEvent("Show Exam List Teacher");
            event.addAttribute("user", SessionManager.getInstance().
                getLogged_in_user());
            // we also capture the communication category
            SessionManager.getInstance().analytics.getEventClient().recordEvent(event);
        } catch (NullPointerException exception)    {
            System.out.println("flopped in creating analytics Show Exam List Teacher");
        } catch (Exception exception)   {
            System.out.println("flopped in creating analytics Show Exam List Teacher");
        }

        // get the list of subjects for which at least one test has been conducted
        String tag = "ExamList";
        final ArrayList<String> exam_title_list = new ArrayList<>();
        final ArrayList<String> exam_id_list = new ArrayList<>();
        final ArrayList<String> exam_type_list = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter(this,
            android.R.layout.simple_list_item_single_choice, exam_title_list);

        ListView listView = findViewById(R.id.exam_list_teacher);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        final String server_ip = MiscFunctions.getInstance().
            getServerIP(this.getApplicationContext());
        final String teacher = SessionManager.getInstance().getLogged_in_user();
        final String url =  server_ip +
            "/academics/get_exam_list_teacher/" + teacher + "/";
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            String id = jo.getString("id");
                            exam_id_list.add(id);

                            String title = jo.getString("title");
                            exam_title_list.add(title);

                            String exam_type = jo.getString("exam_type");
                            exam_type_list.add(exam_type);

                            adapter.notifyDataSetChanged();
                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch the list of students");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the list of students");
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
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    }  else if (error instanceof ServerError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(),
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }

                    // TODO Auto-generated method stub
                }
            });
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        com.classup.AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> view, View v, int position, long id) {
                selected_exam_id = exam_id_list.get(position);
                selected_exam_title = exam_title_list.get(position);
                select_exam_type = exam_type_list.get(position);
            }
        });
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu m) {
        // Inflate the menu; this adds items to the action bar if it is present.
        m.add(0, 0, 0, "Next").
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (selected_exam_id.equals("nil")) {
            Toast toast = Toast.makeText(this,
                "Please select an Exam", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            switch (sender) {
                case "scheduleTest":
                    Intent intent = new Intent(this, SelectClass.class);
                    intent.putExtra("sender", "scheduleTest");
                    intent.putExtra("exam_id", selected_exam_id);
                    intent.putExtra("exam_title", selected_exam_title);
                    intent.putExtra("exam_type", select_exam_type);
                    startActivity(intent);
                    break;
                case "manageTest":
                    Intent intent1 = new Intent(this, TestManagerActivity.class);
                    intent1.putExtra("sender", "scheduleTest");
                    intent1.putExtra("exam_id", selected_exam_id);
                    intent1.putExtra("exam_title", selected_exam_title);
                    intent1.putExtra("exam_type", select_exam_type);
                    startActivity(intent1);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
