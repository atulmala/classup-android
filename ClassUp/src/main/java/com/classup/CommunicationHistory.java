package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommunicationHistory extends AppCompatActivity {
    String server_ip;
    String url;
    String coming_from;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_history);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

        context = getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(context);

        final ArrayList<CommunicationSource> communication_list = new ArrayList<>();
        ListView listView = findViewById(R.id.communication_list);
        final CommunicationHistoryAdapter adapter =
                new CommunicationHistoryAdapter(this, communication_list);
        listView.setAdapter(adapter);

        // retrieve the message history for this user
        server_ip = MiscFunctions.getInstance().getServerIP(context);
        String user = SessionManager.getInstance().getLogged_in_user();
        Intent intent = getIntent();
        coming_from = intent.getStringExtra("coming_from");
        String url = server_ip;
        switch (coming_from)    {
            case "parent":
                url = server_ip + "/operations/retrieve_sms_history/" + user + "/";
                break;
            case "teacher":
                url = server_ip + "/teachers/circulars/" + user + "/Admin/";
                break;
        }


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length() < 1)   {
                            Toast.makeText(context, "Communication History is blank.",
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

                                communication_list.add(new CommunicationSource(id,
                                        ddmmyyyy, message));
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
                        // 12/09/17 - Now we are building the custom
                        // Analysis via AWS
                        try {
                            String event_type = "Communication History";
                            if (coming_from.equals("teacher"))
                                event_type = "Circulars";
                            AnalyticsEvent event = SessionManager.getInstance().
                                            analytics.getEventClient().
                                            createEvent(event_type);
                            event.addAttribute("user", SessionManager.getInstance().
                                    getLogged_in_user());
                            SessionManager.getInstance().analytics.getEventClient().
                                    recordEvent(event);
                        } catch (NullPointerException exception)    {
                            System.out.println("flopped in creating " +
                                    "analytics Communication History");
                        } catch (Exception exception)   {
                            System.out.println("flopped in " +
                                    "creating analytics Communication History");
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
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, "InventoryList");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }
}
