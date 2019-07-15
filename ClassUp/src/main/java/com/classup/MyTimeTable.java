package com.classup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MyTimeTable extends AppCompatActivity {
    String server_ip;
    String url;
    String coming_from;
    String day;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_time_table);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));


        context = getApplicationContext();
        server_ip = MiscFunctions.getInstance().getServerIP(context);

        final ArrayList<TTSource> tt_list = new ArrayList<>();
        Intent intent = getIntent();
        ListView listView = findViewById(R.id.list_view_tt);
        day = intent.getStringExtra("day");
        TextView d = findViewById(R.id.txt_day_of_week);
        System.out.println("day = " + d);
        String heading = "My Time Table for " + day;
        d.setText(heading);
        coming_from = intent.getStringExtra("coming_from");
        final TimeTableAdapter adapter = new TimeTableAdapter(this, tt_list, coming_from);
        listView.setAdapter(adapter);

        server_ip = MiscFunctions.getInstance().getServerIP(context);
        String school_id = SessionManager.getInstance().getSchool_id();
        String user = SessionManager.getInstance().getLogged_in_user();


        String url = server_ip;

        switch (coming_from)    {
            case "teacher":
                url = server_ip + "/time_table/get_time_table/" + school_id + "/teacher/" + user;
                break;
            case "student":
                String stu_id = intent.getStringExtra("student_id");
                System.out.println("stu_id = " + stu_id);
                url = server_ip + "/time_table/get_time_table/na/student/" + stu_id;
                break;
        }

        url += "/" + day +"/";
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if(response.length() < 1)   {
                        Toast.makeText(context, "Time Table not uploaded by School",
                            Toast.LENGTH_LONG).show();
                    }
                    List free_periods = new ArrayList();
                    free_periods.add("1"); free_periods.add("2"); free_periods.add("3");
                    free_periods.add("4"); free_periods.add("5"); free_periods.add("6");
                    free_periods.add("7"); free_periods.add("8");

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            String the_class = jo.getString("the_class");
                            String section = jo.getString("section");
                            String subject = jo.getString("subject");
                            String period = jo.getString("period");
                            if(free_periods.contains(period))   {
                                free_periods.remove(period);
                                System.out.println("free_periods = " + free_periods);
                            }
                            TTSource tt = new TTSource(the_class, period, section, subject);
                            if (!tt_list.contains(tt))
                                tt_list.add(new TTSource(the_class, period, section, subject));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException je) {
                            System.out.println("Ran into JSON exception " +
                                "while trying to fetch time table");
                            je.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Caught General exception " +
                                "while trying to fetch the list of students");
                            e.printStackTrace();
                        }
                    }
                    String today_free_period = "Today's free Periods: ";
                    Iterator<String> iterator = free_periods.iterator();
                    while (iterator.hasNext())  {
                        today_free_period += iterator.next() + ", ";
                    }
                    today_free_period = today_free_period.substring(0,
                        today_free_period.length()-2);
                    tt_list.add(new TTSource(today_free_period, "", "", ""));
                    adapter.notifyDataSetChanged();

                    // 12/09/17 - Now we are building the custom
                    // Analysis via AWS
                    try {
                        String event_type = "Time Table";
                        AnalyticsEvent event = SessionManager.
                            analytics.getEventClient().
                            createEvent(event_type);
                        event.addAttribute("user", SessionManager.getInstance().
                            getLogged_in_user());
                        SessionManager.analytics.getEventClient().
                            recordEvent(event);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating " +
                            "analytics Time Table");
                    } catch (Exception exception)   {
                        System.out.println("flopped in " +
                            "creating analytics Time Table");
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
