package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TeacherMessageRecord extends AppCompatActivity {
    String server_ip;
    String url;

    Context context;
    final ArrayList<MessageSource> messge_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_message_record);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        this.setTitle("Message History");

        context = getApplicationContext();
        String user = SessionManager.getInstance().getLogged_in_user();
        server_ip = MiscFunctions.getInstance().getServerIP(context);
        ListView listView = findViewById(R.id.list_teacher_message_record);
        final TeacherMessageAdapter adapter = new TeacherMessageAdapter(this, messge_list);
        listView.setAdapter(adapter);

        String url = server_ip + "/teachers/message_list/" + user + "/";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if(response.length() < 1)   {
                        Toast.makeText(context, "Message History is blank.",
                            Toast.LENGTH_LONG).show();
                    }
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            // though we have nothing to do with id it's a good idea to keep it
                            String id = jo.getString("id");
                            // get the name of the student. We need to join first and last names
                            String date = jo.getString("date");
                            String yy = date.substring(0, 4);
                            String month = date.substring(5, 7);
                            String dd = date.substring(8, 10);
                            String ddmmyyyy = dd + "/" + month + "/" + yy;

                            String message = jo.getString("message");
                            String sent_to = jo.getString("sent_to");
                            String the_class = jo.getString("the_class");
                            String section = jo.getString("section");

                            if (!the_class.equals("null"))
                                sent_to += " (" + the_class + "-" + section +")";
                            String activity_group = jo.getString("activity_group");

                            String teacher = jo.getString("teacher");

                            messge_list.add(new MessageSource(id, ddmmyyyy, message, sent_to,
                                the_class, section, activity_group, teacher));
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

                    // Analysis via AWS
                    try {
                        AnalyticsEvent event = SessionManager.
                            analytics.getEventClient().
                            createEvent("Teacher Message Record");
                        event.addAttribute("user", SessionManager.getInstance().
                            getLogged_in_user());
                        SessionManager.analytics.getEventClient().
                            recordEvent(event);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating " +
                            "analytics Teacher Message Record");
                    } catch (Exception exception)   {
                        System.out.println("flopped in " +
                            "creating analytics Teacher Message Record");
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
                        Toast.makeText(context, "Slow network connection, please try later",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(context,
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(context,
                            "Slow network connection or No internet connectivity",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        //TODO
                    }
                    // TODO Auto-generated method stub
                }
            });
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, "InventoryList");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String message_id = messge_list.get(i).getId();
                Intent intent = new Intent(context, TeacherMessageReceivers.class);
                intent.putExtra("message_id", message_id);
                startActivity (intent);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().pauseSession();
            SessionManager.analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.analytics != null) {
            SessionManager.analytics.getSessionClient().resumeSession();
        }
    }
}
