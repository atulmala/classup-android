package com.classup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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

public class ActivityMembers extends AppCompatActivity {
    final Activity activity = this;
    String server_ip;
    String url;

    final ArrayList<AttendanceListSource> members_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));


        String group_name = getIntent().getStringExtra("group_name");
        String title = "Members List for " + group_name;
        this.setTitle(title);


        ListView listView = findViewById(R.id.list_activity_members);
        final ActivityMembersAdapter adapter = new ActivityMembersAdapter(activity, members_list);
        listView.setAdapter(adapter);

        server_ip = MiscFunctions.getInstance().getServerIP(this);
        String group_id = getIntent().getStringExtra("group_id");

        String url = server_ip + "/activity_groups/get_activity_group_members/" + group_id + "/";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if(response.length() < 1)   {
                        Toast toast = Toast.makeText(getApplicationContext(),
                            "No Members in this Group!",
                            Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jo = response.getJSONObject(i);

                            String sr_no = Integer.toString(i+1);
                            String f_name = jo.getString("fist_name");
                            String l_name = jo.getString("last_name");
                            String current_class = jo.getString("current_class");
                            String section = jo.getString("current_section");

                            String full_name = sr_no + ".    " + f_name + " " + l_name +
                                " (" + current_class + "-" + section + ")";
                            if (sr_no.length() > 1)
                                full_name = sr_no + ".  " + f_name + " " + l_name +
                                    " (" + current_class + "-" + section + ")";


                            members_list.add (new AttendanceListSource ("n/a", full_name,
                                "n/a", "n/a"));
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
                        AnalyticsEvent event = SessionManager.getInstance().
                            analytics.getEventClient().
                            createEvent("Activity Group Members");
                        event.addAttribute("user", SessionManager.getInstance().
                            getLogged_in_user());
                        SessionManager.getInstance().analytics.getEventClient().
                            recordEvent(event);
                    } catch (NullPointerException exception)    {
                        System.out.println("flopped in creating " +
                            "analytics Activity Group Members");
                    } catch (Exception exception)   {
                        System.out.println("flopped in " +
                            "creating analytics Activity Group Members");
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
                            "Slow network connection, please try later",
                            Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
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
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, "Arrangement List");


    }
}
