package com.classup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OnlineClasses extends AppCompatActivity {
    final Activity activity = this;
    String tag = "Online Classes";
    String server_ip;
    String school_id;
    String sender;
    String student_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_classes);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        final Context c = this.getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(c);
        school_id = SessionManager.getInstance().getSchool_id();
        String logged_in_user = SessionManager.getInstance().getLogged_in_user();

        final ArrayList<OnlineClassSource> lesson_list = new ArrayList<>();

        ListView listView = findViewById(R.id.online_classes);
        listView.setLongClickable(true);
        String url1 = "/academics/";
        String retrieval_message = "Please wait...";

        sender = getIntent().getStringExtra("sender");
        switch (sender) {
            case "parent":
                this.setTitle("Online Class List");
                retrieval_message = "Retrieving Online Classes. Please wait...";
                student_id = getIntent().getStringExtra("student_id");
                url1 = server_ip + "/lectures/get_student_lectures/" + student_id + "/?format=json";
                break;

            case "teacher":
                String teacher = logged_in_user;
                this.setTitle("Online Class List");
                retrieval_message = "Retrieving Online Classes. Please wait...";
                url1 = server_ip + "/lectures/get_teacher_lectures/" + teacher + "/?format=json";
                break;
        }

        final String url = url1;
        final OnlineClassesAdapter adapter = new OnlineClassesAdapter(this, lesson_list);
        listView.setAdapter(adapter);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(retrieval_message);
        progressDialog.setCancelable(true);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() < 1) {
                        Toast toast = Toast.makeText(c, "No Online Classes Created",
                            Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);
                            String id = jo.getString("id");

                            String date = jo.getString("creation_date");
                            String yy = date.substring(0, 4);
                            String month = date.substring(5, 7);
                            String dd = date.substring(8, 10);
                            String ddmmyyyy = dd + "/" + month + "/" + yy;

                            String teacher = jo.getString("teacher");

                            String the_class = jo.getString("the_class");

                            String subject = jo.getString("subject");
                            String topic = jo.getString("topic");
                            String youtube_link = jo.getString("youtube_link");

                            String doc_link = jo.getString("pdf_link");

                            // put all the above details into the adapter
                            lesson_list.add(new OnlineClassSource(id, ddmmyyyy, subject,
                                teacher, the_class, topic, doc_link, youtube_link));
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

        // long tap on the list view will delete the homework
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (sender.equals("teacher")) {
                    final String lecture_id = lesson_list.get(i).getId();

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("Are you sure that you want to delete this Online Class? ")
                        .setPositiveButton("Delete Lecture",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String server_ip = MiscFunctions.getInstance().
                                        getServerIP(activity);
                                    String url = server_ip + "/lectures/delete_lecture/" +
                                        lecture_id + "/";
                                    String tag = "LectureDeletion";
                                    StringRequest request = new StringRequest(Request.Method.DELETE,
                                        url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Toast toast = Toast.makeText
                                                    (getApplicationContext(), "Lecture Deleted",
                                                        Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER,
                                                    0, 0);
                                                toast.show();
                                                finish();
                                                overridePendingTransition(0, 0);
                                                startActivity(getIntent());
                                                overridePendingTransition(0, 0);
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast toast = Toast.makeText
                                                    (getApplicationContext(),
                                                        "Lecture could not be Deleted. " +
                                                            "Please try again",
                                                        Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER,
                                                    0, 0);
                                                toast.show();
                                                error.printStackTrace();
                                            }
                                        });
                                    com.classup.AppController.getInstance().
                                        addToRequestQueue(request, tag);
                                }
                            })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                    // Create the AlertDialog object and return it
                    builder.show();
                }
                return true;
            }
        });
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (sender.equals("teacher")) {
            menu.add(0, 0, 0,
                "Create").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, CreateOnlineClass.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}
