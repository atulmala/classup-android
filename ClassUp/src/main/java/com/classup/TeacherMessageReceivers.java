package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
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

public class TeacherMessageReceivers extends AppCompatActivity {
    String server_ip;
    String url;

    Context context;
    final ArrayList<RecepientMessageSource> messge_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_message_receivers);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        this.setTitle("Recepient Details");
        context = getApplicationContext();

        Intent intent = getIntent();
        String message_id = intent.getStringExtra ("message_id");

        server_ip = MiscFunctions.getInstance().getServerIP(context);
        ListView listView = findViewById(R.id.list_teacher_message_recepients);
        final MessageReceiverAdapter adapter = new MessageReceiverAdapter(this, messge_list);
        listView.setAdapter(adapter);

        String url = server_ip + "/teachers/receivers_list/" + message_id + "/";

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
                            String student = jo.getString("student");
                            String full_message = jo.getString("full_message");
                            String status = jo.getString ("status");
                            String extracted = jo.getString("status_extracted");
                            String outcome = jo.getString("outcome");
                            Boolean status_extracted = true;
                            if (extracted.equals("false"))
                                status_extracted = false;
                            messge_list.add(new RecepientMessageSource(id, student, full_message,
                                status_extracted, status, outcome));
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
                            createEvent("Teacher Message Receivers");
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
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, "InventoryList");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String outcome = "Status is awaited";
                Boolean status_extracted = messge_list.get(i).getStatus_extracted();
                if (status_extracted)
                    outcome = messge_list.get(i).getOutcome();
                Toast toast = Toast.makeText(context, outcome, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            }
        });
    }
}
